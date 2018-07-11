import os
import sys
import json
import random
import time
import logging
from zlib import adler32
from hashlib import md5

import tensorflow
from numpy.random import seed as numpy_seed
from keras import Model, backend as K
from keras.utils import plot_model
from keras.layers import Dense, Reshape, Concatenate
from .environment import StatelessEnv
from .parser import build_model as parser_model_build
from .harvester import parse_repositories
from .rl_patches import TrainEpisodeLogger, DQNAgent

from rl.policy import BoltzmannQPolicy
from rl.memory import SequentialMemory
from rl.processors import MultiInputProcessor
from time import gmtime, strftime
import datetime


def main():
    start_time = str(datetime.datetime.now())

    json_conf, short_hash = init()

    agent_folder = 'results/running-%s' % short_hash
    if not os.path.exists(agent_folder):
        os.makedirs(agent_folder)
    
    logging.basicConfig(filename=agent_folder + "/log.log", level=logging.INFO)

    trades_file = agent_folder + "/trades.csv"
    model_image_file = agent_folder + "/model.png"
    weights_file = agent_folder + "weights.h5f"
    
    logging.info("start time: " + start_time + "\n")
    logging.info(json_conf)

    try:
        logging.info("\n\n build model started: " + short_hash + " start time: " + str(datetime.datetime.now()))
        agent, environment = build_model(json_conf)
        logging.info("build model finished: " + short_hash + " finish time: " + str(datetime.datetime.now()))
    except Exception as e:
        print('\n\n  Exception during model build. \n\n ', file=sys.stderr)
        raise e

    if '-d' in sys.argv:  # debug mode
        environment.trades_output = open(temp_trades_file, 'w')

    plot_model(agent.model, to_file=model_image_file.format(pid=os.getpid()))

    try:
        logging.info("\n\n training started: " + short_hash + " start time: " + str(datetime.datetime.now()))
        iterations = train(environment, agent)
        logging.info("training finished: " + short_hash + " finish time: " + str(datetime.datetime.now()))
        logging.info('iterations = {iterations}'.format(iterations=iterations))
    except Exception as e:
        print('\n\n  Exception during training. \n\n ', file=sys.stderr)
        raise e

    environment.trades_output = environment.trades_output or open(temp_trades_file, 'w')

    try:
        logging.info("\n\n validation started: " + short_hash + " start time: " + str(datetime.datetime.now()))
        validation_score = validate(environment, agent)
        logging.info("validation finished: " + short_hash + " finish time: " + str(datetime.datetime.now()))
    except Exception as e:
        print('\n\n  Exception during validation. \n\n ', file=sys.stderr)
        raise e
    
    try:
        logging.info("\n\n testing started: " + short_hash + " start time: " + str(datetime.datetime.now()))
        test_score = test(environment, agent)
        logging.info("testing finished: " + short_hash + " finish time: " + str(datetime.datetime.now()))
    except Exception as e:
        print('\n\n  Exception during test. \n\n ', file=sys.stderr)
        raise e

    print('score = {result}'.format(result=validation_score))
    print('display_score = {result}'.format(result=test_score))
    agent.save_weights(weights_file, overwrite=True)
    environment.trades_output.close()
    
    environment.close()
    K.get_session().close()


def init():
    config_line = sys.stdin.readline()
    config = json.loads(config_line)

    short_hash = md5(config_line.encode('utf-8')).hexdigest()[0:10]
    # numpy_seed(config.get('seed', adler32(config_line.encode('utf-8'), 1337)))

    tensorflow_config = tensorflow.ConfigProto()
    tensorflow_config.gpu_options.allow_growth = True
    tensorflow_config.gpu_options.visible_device_list = "%d" % random.randint(0, 3)
    tensorflow_session = tensorflow.Session(config=tensorflow_config)
    K.set_session(tensorflow_session)

    return config, short_hash


def build_model(config):
    (internal_model, inputs) = parser_model_build(config)

    env = StatelessEnv(inputs, parse_repositories("repositories.json"), "-v" in sys.argv)
    actions_count = len(env.action_space)

    reshaped_outputs = [Reshape((-1,))(output) for output in internal_model.outputs]
    concatenated_outputs = Concatenate(-1)(reshaped_outputs) if len(reshaped_outputs) > 1 else reshaped_outputs[0]
    dense_transform = Dense(actions_count)(concatenated_outputs)
    outer_model = Model(inputs=internal_model.inputs, outputs=dense_transform)

    memory = SequentialMemory(limit=4000, window_length=config['window_length'])
    policy = BoltzmannQPolicy()
    agent = DQNAgent(
        model=outer_model,
        nb_actions=actions_count,
        memory=memory,
        nb_steps_warmup=100,
        target_model_update=0.01,
        policy=policy,
        processor=MultiInputProcessor(nb_inputs=len(inputs)) if len(inputs) > 1 else None,
    )

    agent.compile(optimizer=internal_model.optimizer, metrics=['mae'])

    return agent, env


def validate(env, agent):
    env.set_mode("validation")
    agent.test(env, nb_episodes=1, action_repetition=1, visualize=False)
    agent.reset_states()
    return env.mode.last_result


def test(env, agent):
    env.set_mode("test")
    agent.test(env, nb_episodes=1, action_repetition=1, visualize=False)
    agent.reset_states()
    return env.mode.last_result


def learn(env, agent, learning_episodes):
    env.set_mode("learning")
    agent.fit(
        env,
        nb_steps=env.mode.steps * learning_episodes,
        action_repetition=1,
        visualize=False,
        verbose=0,
        callbacks=[TrainEpisodeLogger()]
    )
    agent.reset_states()


def train(env, agent):
    # Steps
    max_iterations = 15
    iteration_learning_episodes = 2

    # Policy (exploration versus exploitation of actions)
    final_tau = 0.7

    # Early-stopping (will stop if (current - last) / (last - second_to_last) < stopping_difference_ratio)
    stopping_difference_ratio = 0.5
    patience = 2

    # IDEA: Save weights from X iterations ago, and revert to them after early-stopping ends
    # The rationale is that we have to find the exact start of the overfit, otherwise validation/test perf will suffer

    # Internal variables
    iterations = 0
    last_score = 0.0
    last_difference = 1.0
    stopping_iterations = 0
    policy_tau_change = final_tau ** (1 / max_iterations)

    for i in range(max_iterations):
        logging.info("time: " + str(datetime.datetime.now()) + " iteration: " + str(i))
        iterations += 1
        agent.policy.tau *= policy_tau_change

        learn(env, agent, iteration_learning_episodes)

        current_score = validate(env, agent)

        current_difference = current_score - last_score
        if abs(current_difference) < 0.1:
            current_difference = 0.1

        if current_difference / last_difference < stopping_difference_ratio:
            stopping_iterations += 1
            if stopping_iterations > patience:
                break
        else:
            stopping_iterations = 0

        last_score = current_score
        last_difference = current_difference

    return iterations


if __name__ == '__main__':
    main()

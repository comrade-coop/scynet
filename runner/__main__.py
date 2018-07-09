import os
import sys
import json
import random
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


def main():
    real_stdout = sys.stdout
    sys.stdout = sys.stderr  # Trick debug prints to output to stderr

    json_conf, short_hash = init()
    agent, environment = build_model(json_conf)

    temp_trades_file = 'results/%s-trades.csv' % short_hash

    if '-d' in sys.argv:  # debug mode
        environment.trades_output = open(temp_trades_file, 'w')

    temp_model_image_file = 'results/%s-model.png' % short_hash
    plot_model(agent.model, to_file=temp_model_image_file.format(pid=os.getpid()))

    iterations = train(environment, agent)
    print('iterations = {iterations}'.format(iterations=iterations), file=real_stdout)

    environment.trades_output = environment.trades_output or open(temp_trades_file, 'w')

    validation_score = validate(environment, agent)
    test_score = test(environment, agent)

    sign = '1' if test_score > 0 else '0'
    folder_name = sign + "{:010.2f}".format(abs(test_score)) + "-" + short_hash

    if not os.path.exists('results/' + folder_name):
        os.makedirs('results/' + folder_name)

    weights_file = 'results/' + folder_name + '/%s-weights.h5f' % short_hash
    model_image_file = 'results/' + folder_name + '/%s-model.png' % short_hash
    trades_file = 'results/' + folder_name + '/%s-trades.csv' % short_hash

    print('score = {result}'.format(result=validation_score), file=real_stdout)
    print('display_score = {result}'.format(result=test_score), file=real_stdout)

    agent.save_weights(weights_file, overwrite=True)
    os.rename(temp_model_image_file, model_image_file)
    os.rename(temp_trades_file, trades_file)

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
    max_iterations = 200
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

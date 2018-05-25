import os
import sys
import math
import json
import random
from zlib import adler32
from hashlib import md5

import tensorflow
from numpy.random import seed as numpy_seed
from keras import Model, backend as K
from keras.layers import Dense, Reshape, Concatenate
from .environment import StatelessEnv
from .parser import buildModel
from .rl_patches import PolicyCallback, TrainEpisodeLogger, DQNAgent

from rl.policy import BoltzmannQPolicy
from rl.memory import SequentialMemory
from rl.processors import MultiInputProcessor

real_stdout = sys.stdout
sys.stdout = sys.stderr  # Trick debug prints to output to stderr


config_line = sys.stdin.readline()
config = json.loads(config_line)

short_hash = md5(config_line.encode('utf-8')).hexdigest()[0:10]
#numpy_seed(config.get('seed', adler32(config_line.encode('utf-8'), 1337)))  # Ensure same results for a given chromosome


weigths_file = 'results/weights-%s.h5f' % short_hash
model_image_file = 'results/model-%s.png' % short_hash


tensorflow_config = tensorflow.ConfigProto()
tensorflow_config.gpu_options.allow_growth = True
tensorflow_config.gpu_options.visible_device_list = "%d" % random.randint(0, 3)
tensorflow_session = tensorflow.Session(config=tensorflow_config)
K.set_session(tensorflow_session)


(internal_model, inputs) = buildModel(config)

env = StatelessEnv(inputs, "runner/signals/", "-v" in sys.argv)
actions_count = len(env.action_space)


reshaped_outputs = [Reshape((-1,))(output) for output in internal_model.outputs]
concatenated_outputs = Concatenate(-1)(reshaped_outputs) if len(reshaped_outputs) > 1 else reshaped_outputs[0]
dense_transform = Dense(actions_count)(concatenated_outputs)
outer_model = Model(inputs=internal_model.inputs, outputs=dense_transform)

if True:
    from keras.utils import plot_model
    plot_model(outer_model, to_file=model_image_file.format(pid=os.getpid()))


# Finally, we configure and compile our agent. You can use every built-in Keras optimizer and
# even the metrics!
memory = SequentialMemory(limit=4000, window_length=1)
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
agent.compile(internal_model.optimizer, metrics=['mae'])

validation_score = None
test_score = None

if os.path.isfile(weigths_file):
    agent.load_weights(weigths_file)
else: # For some reason, learning from learned weights didn't work in the past

    # Steps
    max_iterations = 200
    iteration_learning_episodes = 2
    # Policy (exploration versus exploitation of actions)
    final_tau = 0.7
    # Early-stopping (will stop if (current - last) / (last - second_to_last) < stopping_difference_ratio)
    stopping_difference_ratio = 0.5
    patience = 2
    ## IDEA: Save weigths from X iterations ago, and revert to them after early-stopping ends
    ## The rationale is that we have to find the exact start of the overfit, otherwise validation/test perf will suffer

    # Internal variables
    iterations = 0
    last_score = 0.0
    last_difference = 1.0
    stopping_episodes = 0
    policy_tau_change = final_tau ** (1 / max_iterations)

    for i in range(max_iterations):
        iterations += 1
        policy.tau *= policy_tau_change

        env.mode = "learning"
        agent.fit(
            env,
            nb_steps=env.learning_rows * iteration_learning_episodes,
            action_repetition=1,
            visualize=False,
            verbose=0,
            callbacks=[TrainEpisodeLogger()]
        )
        env.finish_episode()

        env.mode = "validation"
        agent.test(env, nb_episodes=1, action_repetition=1, visualize=False)

        current_score = env.last_episode_result
        current_difference = current_score - last_score
        if abs(current_difference) < 0.1: current_difference = 0.1

        # print("Debug:", current_score, last_score, current_difference, last_difference)
        # print("Ratio is:", current_difference / last_difference)

        if current_difference / last_difference < stopping_difference_ratio:
            stopping_episodes += 1
            if stopping_episodes > patience:
                break
        else:
            stopping_episodes = 0

        last_score = current_score
        last_difference = current_difference

    validation_score = last_score

    print('iterations = {iterations}'.format(iterations=iterations), file=real_stdout)

    agent.save_weights(weigths_file, overwrite=True)


# agent.model.reset_states()  # Unneeded, as it is done automatically by keras-rl

if validation_score is None:
    env.mode = "validation"
    agent.test(env, nb_episodes=1, action_repetition=1, visualize=False)
    validation_score = env.last_episode_result

if test_score is None:
    env.mode = "test"
    agent.test(env, nb_episodes=1, action_repetition=1, visualize=False)
    test_score = env.last_episode_result

print('score = {result}'.format(result=validation_score), file=real_stdout)
print('display_score = {result}'.format(result=test_score), file=real_stdout)

env.close()
K.get_session().close()

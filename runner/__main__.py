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
numpy_seed(config.get('seed', adler32(config_line.encode('utf-8'), 1337)))  # Ensure same results for a given chromosome


weigths_file = 'results/weights-%s.h5f' % short_hash
model_image_file = 'results/model-%s.png' % short_hash


tensorflow_config = tensorflow.ConfigProto()
tensorflow_config.gpu_options.allow_growth = True
tensorflow_config.gpu_options.visible_device_list = "%d" % random.randint(0, 3)
tensorflow_session = tensorflow.Session(config=tensorflow_config)
K.set_session(tensorflow_session)


if False and all(layer['type'] == 'Input' for layer in config['layers']):
    raise NotImplementedError('All layers are input: ' + str([layer['type'] for layer in config['layers']]))


(internal_model, inputs) = buildModel(config)

env = StatelessEnv(inputs, "runner/signals/", real_stdout)
actions_count = len(env.action_space)


reshaped_outputs = [Reshape((-1,))(output) for output in internal_model.outputs]
concatenated_outputs = Concatenate(-1)(reshaped_outputs) if len(reshaped_outputs) > 1 else reshaped_outputs[0]
dense_transform = Dense(actions_count)(concatenated_outputs)
outer_model = Model(inputs=internal_model.inputs, outputs=dense_transform)

if False:
    from keras.utils import plot_model
    plot_model(outer_model, to_file=model_image_file.format(pid=os.getpid()))


# Finally, we configure and compile our agent. You can use every built-in Keras optimizer and
# even the metrics!
memory = SequentialMemory(limit=4000, window_length=1)
policy = BoltzmannQPolicy()
dqn = DQNAgent(
    model=outer_model,
    nb_actions=actions_count,
    memory=memory,
    nb_steps_warmup=100,
    target_model_update=0.01,
    policy=policy,
    processor=MultiInputProcessor(nb_inputs=len(inputs)) if len(inputs) > 1 else None,
)
dqn.compile(internal_model.optimizer, metrics=['mae'])

if os.path.isfile(weigths_file):
    dqn.load_weights(weigths_file)
else:
    # dqn.load_weights(weigths_file)
    step_count = 30 * 1000
    policy_steps = 1000
    final_tau = 0.5

    dqn.fit(
        env,
        nb_steps=step_count,
        action_repetition=1,
        visualize=False,
        verbose=0,
        callbacks=[PolicyCallback(policy, 0.99, step_count * 10 ** (-math.log(final_tau, 10) / policy_steps)), TrainEpisodeLogger()]
    )

    env.finish_episode()

    dqn.save_weights(weigths_file, overwrite=True)

# dqn.test(env, nb_episodes=1, action_repetition=1, visualize=False)
env.validation = True
dqn.test(env, nb_episodes=1, action_repetition=1, visualize=False)

env.close()
K.get_session().close()

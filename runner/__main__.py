import os
import sys
import math
import json

from keras import Model
from keras.layers import Dense, Reshape, Concatenate
from .environment import StatelessEnv
from .parser import buildModel
from .rl_callbacks import PolicyCallback, TrainEpisodeLogger

from rl.agents.dqn import DQNAgent
from rl.policy import BoltzmannQPolicy
from rl.memory import SequentialMemory
from rl.processors import MultiInputProcessor

real_stdout = sys.stdout
sys.stdout = sys.stderr  # Trick debug prints to output to stderr

line = sys.stdin.readline()
config = json.loads(line)


(internal_model, inputs) = buildModel(config)

env = StatelessEnv(inputs, "runner/signals/", real_stdout)
actions_count = len(env.action_space)

reshaped_outputs = [Reshape((-1,))(output) for output in internal_model.outputs]
concatenated_outputs = Concatenate(-1)(reshaped_outputs) if len(reshaped_outputs) > 1 else reshaped_outputs[0]
dense_transform = Dense(actions_count)(concatenated_outputs)
outer_model = Model(inputs=internal_model.inputs, outputs=dense_transform)

if True:
    from keras.utils import plot_model
    plot_model(outer_model, to_file='model-{pid}-.png'.format(pid=os.getpid()))

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

if False and os.path.isfile('dqn_learner_weights.h5f'):
    dqn.load_weights('dqn_learner_weights.h5f')
elif True:
    # try:
    #     if weights_file is not None and os.path.isfile(weights_file):
    #         dqn.load_weights(weights_file)
    # except Exception as e:
    #     print(e)
    #     pass
    step_count = 100 * 1000
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

    # dqn.save_weights('dqn_learner_weights.h5f', overwrite=True)

# dqn.test(env, nb_episodes=1, action_repetition=1, visualize=False)
env.validation = True
dqn.test(env, nb_episodes=1, action_repetition=1, visualize=False)

env.close()

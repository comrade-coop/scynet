import os
import sys
import json

from .environment import StatelessEnv
from .parser import buildModel
from .rl_callbacks import PolicyCallback, TrainEpisodeLogger

from rl.agents.dqn import DQNAgent
from rl.policy import BoltzmannQPolicy
from rl.memory import SequentialMemory
from rl.processors import MultiInputProcessor

line = sys.stdin.readline()
config = json.loads(line)


(model, inputs) = buildModel(config)

env = StatelessEnv(inputs, "runner/signals/")
actions_count = len(env.action_space)


# Finally, we configure and compile our agent. You can use every built-in Keras optimizer and
# even the metrics!
memory = SequentialMemory(limit=40000, window_length=1)
policy = BoltzmannQPolicy()
dqn = DQNAgent(
    model=model,
    nb_actions=actions_count,
    memory=memory,
    nb_steps_warmup=100,
    target_model_update=0.01,
    policy=policy,
    processor=MultiInputProcessor(nb_inputs=len(inputs)),
)
dqn.compile(model.optimizer, metrics=['mae'])

if False and os.path.isfile('dqn_learner_weights.h5f'):
    dqn.load_weights('dqn_learner_weights.h5f')
else:
    # try:
    #     if weights_file is not None and os.path.isfile(weights_file):
    #         dqn.load_weights(weights_file)
    # except Exception as e:
    #     print(e)
    #     pass

    dqn.fit(
        env,
        nb_steps=2000 * 20,
        action_repetition=1,
        visualize=False,
        verbose=0,
        callbacks=[PolicyCallback(policy, 0.99, 1000), TrainEpisodeLogger()]
    )

    env.reset()

    # dqn.save_weights('dqn_learner_weights.h5f', overwrite=True)

dqn.test(env, nb_episodes=1, action_repetition=1, visualize=False)

env.close()

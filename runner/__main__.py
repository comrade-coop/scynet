import os
import sys
import json

from environment import StatelessEnv
from parser import buildModel
from preprocessor import MeanStdevPreprocessor

from rl.agents.dqn import DQNAgent
from rl.policy import BoltzmannQPolicy
from rl.memory import SequentialMemory
from rl.callbacks import Callback
from rl.processors import MultiInputProcessor

line = sys.stdin.readline()
print("LINE IS", line)
config = json.loads(line)

preprocessors = [MeanStdevPreprocessor(100, config['window_length']), MeanStdevPreprocessor(100, config['window_length'])]

env = StatelessEnv(preprocessors, config, "runner/signals/")


window_length = 3
actions_count = len(env.action_space)

# Next, we build a very simple model.
model = buildModel(config)


class PolicyCallback(Callback):
    def __init__(self, policy, factor, steps, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.policy = policy
        self.factor = factor
        self.steps = steps
        self.steps_left = 0

    def on_action_end(self, action, logs={}):
        self.steps_left -= 1
        if self.steps_left < 0:
            self.steps_left += self.steps
            self.policy.tau *= self.factor


# Finally, we configure and compile our agent. You can use every built-in Keras optimizer and
# even the metrics!
memory = SequentialMemory(limit=40000, window_length=1)
policy = BoltzmannQPolicy()
dqn = DQNAgent(
    model=model,
    nb_actions=actions_count,
    memory=memory,
    nb_steps_warmup=100,
    target_model_update=1e-2,
    policy=policy,
    processor=MultiInputProcessor(nb_inputs=len(preprocessors)),
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
        verbose=2,
        callbacks=[PolicyCallback(policy, 0.99, 1000)]
    )

    env.reset()

    # dqn.save_weights('dqn_learner_weights.h5f', overwrite=True)

dqn.test(env, nb_episodes=1, action_repetition=1, visualize=False)

env.close()

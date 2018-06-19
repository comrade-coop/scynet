import numpy
from rl.agents.dqn import DQNAgent as DQNAgentOrig
from rl.callbacks import Callback, TrainEpisodeLogger as TrainEpisodeLoggerOrig
import collections


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


class TrainEpisodeLogger(TrainEpisodeLoggerOrig):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

    def on_episode_end(self, episode, logs):
        def flatten(l):
            for el in l:
                if isinstance(el, collections.Iterable) and not isinstance(el, (str, bytes)):
                    yield from flatten(el)
                else:
                    yield el
        self.observations[episode] = list(flatten(self.observations[episode]))

        super().on_episode_end(episode, logs)


class DQNAgent(DQNAgentOrig):
    def process_state_batch(self, batch):
        if self.processor is None:
            return numpy.array(batch)
        return self.processor.process_state_batch(batch)

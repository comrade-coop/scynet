import math

from .spaces import TupleSpace, BoxSpace, SetSpace


def collapse_single_item(li):
    if len(li) == 1:
        return li[0]
    else:
        return li


class RLEnvironment():
    action_space = SetSpace([0, 1])

    def __init__(self, trainer):
        self.trainer = trainer

        self.observation_space = TupleSpace([BoxSpace(-100, 100, subshape) for subshape in trainer.signal.shape])

        self.same_action_ticks_limit = 500
        self.same_action_feedback_exp = 1.005

        self.last_action = 0
        self.same_action_ticks = 0
        self.last_result = None

        self.episode = 'learning'

    def set_episode(self, episode):
        self.episode = episode

    def reset(self):
        if isinstance(self.episode, str):
            self.training_session = self.trainer.start_session(self.episode)
        else:
            self.training_session = self.episode  # HACK: Hope the passed in episode is a proper session
        self.last_action = 0
        self.same_action_ticks = 0

        (date, observation) = self.training_session.get_observation()

        return collapse_single_item(observation)

    def step(self, action):
        self.training_session.give_action(['sell', 'buy'][action])
        (date, observation) = self.training_session.get_observation()

        feedback = self.training_session.get_feedback()

        if action != self.last_action:
            self.last_action = action
            self.same_action_ticks = 0
        else:
            self.same_action_ticks += 1
            if self.same_action_ticks > self.same_action_ticks_limit:
                feedback -= self.same_action_feedback_exp ** (self.same_action_ticks - self.same_action_ticks_limit) - 1

        feedback = math.tanh(feedback / 10) * 10

        if self.training_session.is_observation_final():
            self.last_result = self.training_session.get_result()

        return (collapse_single_item(observation), feedback, self.training_session.is_observation_final(), {})

    def close(self):
        self.training_session = None

    def seed(self, seed=None):
        pass

    def configure(self, *args, **kwargs):
        pass

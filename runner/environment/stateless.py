import sys
import iterools
import math

from .spaces import TupleSpace, BoxSpace, SetSpace


class StatelessEnv():
    def __init__(self, normalizer, config, data_iterable, *args, **kwargs):
        self.data_iterable = data_iterable

        self.normalizer = normalizer

        self.action_space = SetSpace([0, 1])

        spaces = [BoxSpace(-1, 1, tuple(layer['config']['shape'])) for layer in config['layers'] if layer['type'] == 'Input']
        self.observation_space = TupleSpace(spaces)

        self.debug_i = 0
        self.reset()  # Adds more properties

    def step(self, action):
        if self.debug_i % 50 == 0:
            print('Env step %s' % self.debug_i, file=sys.stderr)
            self.debug_i += 1

        feedback = 0.0

        if action != self.last_action:
            price = self.data_iterator.get_base().close

            if action == 'sell':
                print(
                    'cycle {ticks: >4}, buy {self.buy_price: >8.2f}, sell {sell_price: >8.2f}, diff {price_diff: >+8.2f}'.format(
                        action='buy' if action == 1 else 'sell',
                        ticks=self.same_action_ticks,
                        buy_price=self.buy_price,
                        sell_price=price,
                        price_diff=price - self.buy_price,
                    ),
                    file=sys.stderr)

            self.last_action = action
            self.same_action_ticks = 0

            if action == 1:
                self.buy_price = price
                # feedback -= price * 0.0015

            elif action == 0:
                feedback += price - self.buy_price
                feedback -= self.buy_price * 0.0015
                feedback -= price * 0.0025
        else:
            self.same_action_ticks += 1
            if self.same_action_ticks > self.same_action_ticks_limit:
                feedback -= self.same_action_feedback_exp ** (self.same_action_ticks - self.same_action_ticks_limit) - 1

        feedback = math.tanh(feedback / 10) * 10

        try:
            return (
                self.normalizer.normalize(self.data_iterator.__next__()),
                feedback,
                False,
                {})
        except StopIteration:
            # return (self.reset(), 0.0, True, {})
            return (self.observation_space.sample(), 0.0, True, {})

    def reset(self):
        self.data_iterator = iter(self.data_iterable)
        self.last_action = 0
        self.buy_price = None
        self.same_action_ticks = 0
        self.same_action_ticks_limit = 500
        self.same_action_feedback_exp = 1.005

        self.normalizer.reset()
        try:
            self.normalizer.fit(iterools.islice(self.data_iterator, self.normalizer.prefetch_rows))
        except StopIteration:
            raise Exception('Data finished before we could prefetch for normalization')

        return self.normalizer.normalize(self.data_iterator.__next__())

    def close(self):
        self.data_iterator = None

    def seed(self, seed=None):
        pass

    def configure(self, *args, **kwargs):
        pass

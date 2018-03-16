import sys
from os import path
import itertools
import math

from .spaces import TupleSpace, BoxSpace, SetSpace
from .signal_reader import SignalReader


class RawIndicator():
    def __init__(self, target):
        self.target = target

    def calculate_frame(self, data):
        return data[self.target]


class StatelessEnv():
    def __init__(self, preprocessors, config, signals_base_path, *args, **kwargs):
        self.preprocessors = preprocessors

        self.action_space = SetSpace([0, 1])
        self.observation_space = None

        self.data = None
        self.data_iterator = None
        self.last_action = None
        self.buy_price = None
        self.same_action_ticks = None
        self.same_action_ticks_limit = 500
        self.same_action_feedback_exp = 1.005

        self.debug_i = 0
        self.collect_data(config, signals_base_path)
        self.reset()

    def collect_data(self, config, signal_base_dir):
        self.data = []
        self.observation_space = ()

        spaces = []

        self.data.append(SignalReader(path.join(signal_base_dir, 'base-close.csv'), target_column='close').read_all())

        for layer in config['layers']:
            if layer['type'] == 'Input':
                spaces.append(BoxSpace(-100, 100, (config['window_length'],) + tuple(layer['config']['shape'])))
                result = None
                source = layer['config']['source']
                if source['type'] == 'MarketSource':
                    result = SignalReader(path.join(signal_base_dir, 'base-%s.csv' % source['config']['signal']), target_column=source['config']['signal']).read_all()

                if result is None:
                    raise NotImplementedError("Unsupported source config: " + source)

                self.data.append(result)

        self.observation_space = TupleSpace(spaces)
        assert(len(self.data) == len(self.preprocessors) + 1)

    def reset(self):
        self.data_iterator = zip(*[iter(input_data.itertuples()) for input_data in self.data])
        self.last_action = 0
        self.buy_price = None
        self.same_action_ticks = 0

        for preprocessor, input_data in zip(self.preprocessors, self.data[1:]):
            preprocessor.reset_state()
            preprocessor.init((thing[1:] for thing in itertools.islice(input_data.itertuples(), preprocessor.prefetch_tick_count)))

        next_item = self.data_iterator.__next__()[1:]

        return [preprocessor.append_and_preprocess(input_row[1:]) for preprocessor, input_row in zip(self.preprocessors, next_item)]

    def step(self, action):
        if self.debug_i % 50 == 0:
            print('Env step %s' % self.debug_i, file=sys.stderr)
        self.debug_i += 1

        try:
            next_item = self.data_iterator.__next__()
        except StopIteration:
            return (self.observation_space.sample(), 0.0, True, {})

        price = next_item[0].close
        next_item = next_item[1:]
        feedback = 0.0

        if action != self.last_action:

            if action == 0:
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
                # feedback -= self.buy_price * 0.0015
                # feedback -= price * 0.0025
        else:
            self.same_action_ticks += 1
            if self.same_action_ticks > self.same_action_ticks_limit:
                feedback -= self.same_action_feedback_exp ** (self.same_action_ticks - self.same_action_ticks_limit) - 1

        feedback = math.tanh(feedback / 10) * 10

        return (
            [preprocessor.append_and_preprocess(input_row[1:]) for preprocessor, input_row in zip(self.preprocessors, next_item)],
            feedback,
            False,
            {})

    def close(self):
        self.data_iterator = None

    def seed(self, seed=None):
        pass

    def configure(self, *args, **kwargs):
        pass

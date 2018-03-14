import sys
from os import path
import itertools
import math
import pandas

from .spaces import TupleSpace, BoxSpace, SetSpace
from .signal_reader import SignalReader


class RawIndicator():
    def __init__(self, target):
        self.target = target

    def calculate_frame(self, data):
        return data[self.target]


class StatelessEnv():
    def __init__(self, normalizers, config, signals_base_path, *args, **kwargs):
        self.normalizers = normalizers

        self.action_space = SetSpace([0, 1])

        spaces = [BoxSpace(-1, 1, tuple(layer['config']['shape'])) for layer in config['layers'] if layer['type'] == 'Input']
        self.observation_space = TupleSpace(spaces)

        self.debug_i = 0
        self.collect_data(config, signals_base_path)  # Adds self.data
        self.reset()  # Adds more properties

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

        return (
            [normalizer.append_and_preprocess(list(input_row)[1:]) for normalizer, input_row in zip(self.normalizers, next_item)],
            feedback,
            False,
            {})

    def reset(self):
        self.data_iterator = zip(*[iter(input_data.itertuples()) for input_data in self.data])
        self.last_action = 0
        self.buy_price = None
        self.same_action_ticks = 0
        self.same_action_ticks_limit = 500
        self.same_action_feedback_exp = 1.005

        try:
            for normalizer, input_data in zip(self.normalizers, self.data[1:]):
                normalizer.reset_state()
                normalizer.init(itertools.islice(input_data, normalizer.prefetch_tick_count))
        except StopIteration:
            raise Exception('Data finished before we could prefetch for normalization')

        next_item = self.data_iterator.__next__()[1:]
        return [normalizer.append_and_preprocess(list(input_row)[1:]) for normalizer, input_row in zip(self.normalizers, next_item)]

    def close(self):
        self.data_iterator = None

    def seed(self, seed=None):
        pass

    def configure(self, *args, **kwargs):
        pass

    def collect_data(self, config, signal_base_dir):
        self.data = []
        self.data.append(SignalReader(path.join(signal_base_dir, 'base-close.csv'), target_column='close').read_all())
        for layer in config['layers']:
            if layer['type'] == 'Input':
                result = None
                source = layer['config']['source']
                # if source['type'] == 'talib':
                #     signals['close'] = 'base-close.csv'
                #     signals['open'] = 'base-open.csv'
                #     signals['high'] = 'base-high.csv'
                #     signals['low'] = 'base-low.csv'
                #     signals['volume'] = 'base-volume.csv'
                #     indicators.append(TalibIndicator(indicator.function, *indicator.arguments))
                if source['type'] == 'MarketSource':
                    result = SignalReader(path.join(signal_base_dir, 'base-%s.csv' % source['config']['signal']), target_column=source['config']['signal']).read_all()

                if result is None:
                    raise NotImplementedError("Unsupported source config: " + source)

                self.data.append(result)

        assert(len(self.data) == len(self.normalizers) + 1)

import sys
from os import path
import itertools
import math
import pandas
import numpy

from .spaces import TupleSpace, BoxSpace, SetSpace
from .signal_reader import SignalReader


def collapse_single_item(li):
    if len(li) == 1:
        return li[0]
    else:
        return li


class StatelessEnv():
    def __init__(self, inputs, signals_base_path, interactive, *args, **kwargs):
        self.inputs = inputs

        self.action_space = SetSpace([0, 1])
        self.observation_space = None

        self.mode = "learning"
        self.data = None
        self.validation_data = None
        self.data_iterator = None
        self.buy_price = None
        self.last_action = 0
        self.same_action_ticks = 0
        self.same_action_ticks_limit = 500
        self.test_rows = 365 * 24 // 2  # ½ year
        self.validation_rows = 365 * 24 // 2  # ½ year
        self.learning_rows = None
        self.same_action_feedback_exp = 1.005
        self.interactive = interactive
        self.starting_currency = 1000.0
        self.balance_currency = None
        self.balance_asset = None
        self.last_episode_result = None

        self.debug_i = 0
        self.collect_data(signals_base_path)

    def state_generator(self):
        while True:
            yield (pandas.Timestamp.now(), self.same_action_ticks / 100, self.last_action)

    def data_wrapper(self, columns):
        def _inner():
            data_source = {"learning": self.learning_data, "validation": self.validation_data, "test": self.test_data}[self.mode]
            return (data_source[columns].itertuples())
        return _inner

    def collect_data(self, signal_base_dir):
        self.data = []
        self.preprocessors = [preprocessor for preprocessor, source in self.inputs]

        dataframes_to_merge = []

        # Data comes from blockchain-predictor downloader
        dataframes_to_merge.append(SignalReader(path.join(signal_base_dir, 'cryptocompare_price_data.json')).read_all())
        self.data.append(self.data_wrapper(['close']))

        for preprocessor, source in self.inputs:
            result = None
            if source['from'] == 'local':
                name = source['name'].split('.')
                if name[0] == 'market':
                    if name[1] == 'all':
                        result = self.data_wrapper(['close','high','low','open','volumefrom','volumeto'])
                    else:
                        result = self.data_wrapper(name[1].split(','))
                if name[0] == 'state':
                    result = self.state_generator

            if result is None:
                raise NotImplementedError('Unsupported source config: ' + source)

            self.data.append(result)

        merged_data = pandas.concat(dataframes_to_merge, axis=1)
        self.learning_data = merged_data.iloc[:-self.validation_rows-self.test_rows]
        self.test_data = merged_data.iloc[-self.validation_rows-self.test_rows:-self.test_rows]
        self.validation_data = merged_data.iloc[-self.test_rows:]
        self.learning_rows = self.learning_data.shape[0]

        spaces = []
        for preprocessor, input_data in zip(self.preprocessors, self.data[1:]):
            data_shape = numpy.shape(iter(input_data()).__next__()[1:])
            spaces.append(BoxSpace(-100, 100, (preprocessor.output_window_length,) + data_shape))

        self.observation_space = TupleSpace(spaces)
        assert(len(self.data) == len(self.preprocessors) + 1)

        print('Loaded Data: {learning} training rows, {validation} validation rows, and {test} test rows'.format(
            learning=self.learning_data.shape[0],
            test=self.test_data.shape[0],
            validation=self.validation_data.shape[0],
        ))

    def reset(self):
        self.data_iterator = zip(*[iter(input_data()) for input_data in self.data])
        self.last_action = 0
        self.last_price = 0.0
        self.buy_price = None
        self.same_action_ticks = 0
        self.balance_currency = self.starting_currency
        self.balance_asset = 0.0
        if self.mode == "validation":
            print('Starting validation episode ({date})'.format(date = iter(self.data[0]()).__next__()[0]))
        elif self.mode == "test":
            print('Starting test episode ({date})'.format(date = iter(self.data[0]()).__next__()[0]))
        elif self.mode == "learning":
            learn_from = 0  # int(numpy.random.uniform(0, self.learning_rows - 100))
            learn_to = self.learning_rows  # int(numpy.random.uniform(learn_from + 100, self.learning_rows))
            # self.data_iterator = itertools.islice(self.data_iterator, learn_from, learn_to)
            print('Starting learning episode from {start} to {end} ({range} rows out of {total} rows)'.format(
                start=learn_from,
                end=learn_to,
                range=learn_to - learn_from,
                total=self.learning_rows
            ))

        for preprocessor, input_data in zip(self.preprocessors, self.data[1:]):
            preprocessor.reset_state()
            preprocessor.init((thing[1:] for thing in itertools.islice(input_data(), preprocessor.prefetch_tick_count)))

        next_item = self.data_iterator.__next__()[1:]

        print('', end='')

        return collapse_single_item([preprocessor.append_and_preprocess(input_row[1:]) for preprocessor, input_row in zip(self.preprocessors, next_item)])

    def finish_episode(self):
        if self.data_iterator is None:
            return
        self.balance_currency += self.balance_asset * self.last_price
        self.balance_asset = 0.0
        self.data_iterator = None
        self.last_episode_result = self.balance_currency - self.starting_currency
        print('')  # Makes a newline on stderr
        print('{mode} episode score: {result: >+8.2f}'.format(mode=self.mode.capitalize(), result=self.last_episode_result))

    def step(self, action):
        self.debug_i += 1
        try:
            next_item = self.data_iterator.__next__()
        except StopIteration:
            self.finish_episode()
            return (collapse_single_item(self.observation_space.sample()), 0.0, True, {})

        if self.debug_i % 25 == 0 and self.interactive:
            print(' Step {step: =6} ({date}) ${balance: >7.2f} {state}     \r'.format(
                step=self.debug_i,
                date=next_item[0].Index,
                balance=(0.0 if self.buy_price is None else self.buy_price) * self.balance_asset + self.balance_currency,
                state='bougth' if action == 1 else 'sold  '  # Spaces are important, both need to be same length
            ), end='')

        price = next_item[0].close
        next_item = next_item[1:]
        real_feedback = 0.0
        learning_feedback = 0.0

        if action != self.last_action:

            if action == 0 and False:
                print('cycle {ticks: >4}, buy {buy_price: >8.2f}, sell {sell_price: >8.2f}, diff {price_diff: >+8.2f}'.format(
                    action='buy' if action == 1 else 'sell',
                    ticks=self.same_action_ticks,
                    buy_price=self.buy_price,
                    sell_price=price,
                    price_diff=price - self.buy_price,
                ))

            self.last_action = action
            self.same_action_ticks = 0

            if action == 1:
                self.buy_price = price
                self.balance_asset += self.balance_currency / price
                self.balance_currency = 0.0
                # if self.mode == "validation":
                #     real_feedback -= price * 0.0015

            elif action == 0:
                real_feedback += price - self.buy_price
                self.balance_currency += self.balance_asset * price
                self.balance_asset = 0.0
                # if self.mode == "validation":
                #     # real_feedback -= self.buy_price * 0.0015
                #     real_feedback -= price * 0.0025
        else:
            self.same_action_ticks += 1
            if self.same_action_ticks > self.same_action_ticks_limit:
                learning_feedback -= self.same_action_feedback_exp ** (self.same_action_ticks - self.same_action_ticks_limit) - 1

        self.last_price = price
        if self.mode == "learning":
            feedback = math.tanh((real_feedback + learning_feedback) / 10) * 10
        else:
            feedback = math.tanh(real_feedback / 10) * 10

        return (
            collapse_single_item([preprocessor.append_and_preprocess(input_row[1:]) for preprocessor, input_row in zip(self.preprocessors, next_item)]),
            feedback,
            False,
            {})

    def close(self):
        self.data_iterator = None

    def seed(self, seed=None):
        pass

    def configure(self, *args, **kwargs):
        pass

from os import path
from types import SimpleNamespace as Namespace
import sys
import math
import json

from talib import abstract as talib_abstract
import numpy
import pandas

from scynet_utils.connector import ScynetConnector
from .signal_reader import SignalReader


class DataIterable():
    def __init__(self, readers, indicators):
        self.base = pandas.concat([reader.read_all() for reader in readers], axis=1)
        self.base.fillna(method='pad', inplace=True)
        self.values = pandas.concat([indicator.calculate_frame(self.base).reindex(self.base.index) for indicator in indicators], axis=1)

        self.shape = self.values.shape[1:]

    def __iter__(self):
        return DataIterator(self)


class DataIterator():
    def __init__(self, iterable):
        self.base_iterator = iterable.base.itertuples()
        self.values_iterator = iterable.values.itertuples()
        self.base_row = None

    def __next__(self):
        value_row = None
        while value_row is None or numpy.isnan(list(value_row)).any():
            self.base_row = self.base_iterator.__next__()
            value_row = self.values_iterator.__next__()[1:]
        return value_row

    def get_base(self):
        return self.base_row


class RawIndicator():
    def __init__(self, target):
        self.target = target

    def calculate_frame(self, data):
        return data[self.target]

    def calculate_window(self, window):
        return window[self.target][0]


class TalibIndicator():
    def __init__(self, method, *args):
        self.func = talib_abstract.Function(method)

        params = self.func.get_parameters()
        params.update(zip(params.keys(), args))
        self.func.set_parameters(params)

    def calculate_frame(self, data):
        result = self.func.run(dict([(column, numpy.array(data[column])) for column in data]))
        if isinstance(result, list):
            result = result[0]
        return pandas.Series(result, index=data.index)

    def calculate_window(self, window):
        return self.func.run(dict([(column, window[column]) for column in window]))[-1]


def __main__():
    config_file = open('config.json')
    config = json.load(config_file, object_hook=lambda d: Namespace(**d))
    config_file.close()

    def create_data_iterable():  # TODO: Use new configuration format
        signal_base_dir = path.join(path.dirname(__file__), '../data/signals/')

        signals = {}
        indicators = []
        additional_count = 0
        signals['close'] = 'base-close.csv'
        for indicator in config.inputs:
            if indicator.type == 'talib':
                signals['close'] = 'base-close.csv'
                signals['open'] = 'base-open.csv'
                signals['high'] = 'base-high.csv'
                signals['low'] = 'base-low.csv'
                signals['volume'] = 'base-volume.csv'
                indicators.append(TalibIndicator(indicator.function, *indicator.arguments))
            elif indicator.type == 'market':
                signals[indicator.property] = 'base-%s.csv' % indicator.property
                indicators.append(RawIndicator(indicator.property))
            else:
                additional_count += 1
                signals['%s-%d' % [indicator.type, additional_count]] = indicator.filename
                indicators.append(RawIndicator('%s-%d' % [indicator.type, additional_count]))

        return DataIterable(
            [SignalReader(path.join(signal_base_dir, filename), target_column=column) for column, filename in signals.items()],
            indicators
        )

    data = create_data_iterable()

    connector = ScynetConnector()

    data_iterator = iter([])
    buy_price = None
    last_action = 'sell'
    same_action_ticks = 0
    fitness = 0
    same_action_ticks_limit = 500
    same_action_feedback_exp = 1.005

    while True:
        query = connector.read()
        try:
            if query is None:
                continue
            if hasattr(query, 'close'):
                break
            elif hasattr(query, 'reset'):
                data_iterator = iter(data)  # Reset the iterator
                fitness = 0
                connector.write(values=data_iterator.__next__())
            elif hasattr(query, 'action'):
                feedback = 0.0
                if query.action != last_action:
                    price = data_iterator.get_base().close

                    if query.action == 'sell':
                        print(
                            'cycle {ticks: >4}, buy {buy_price: >8.2f}, sell {sell_price: >8.2f}, diff {price_diff: >+8.2f}'.format(
                                action=query.action,
                                ticks=same_action_ticks,
                                buy_price=buy_price,
                                sell_price=price,
                                price_diff=price - buy_price,
                            ),
                            file=sys.stderr)

                    # if same_action_ticks < same_action_ticks_limit:
                    #     feedback -= same_action_feedback_exp ** ((same_action_ticks_limit - same_action_ticks) ** 2 * math.pi) - 1

                    last_action = query.action
                    same_action_ticks = 0

                    if query.action == 'buy':
                        buy_price = price
                        feedback -= price * 0.0015

                    elif query.action == 'sell':
                        feedback += price - buy_price
                        feedback -= price * 0.0025

                    fitness += feedback
                else:
                    same_action_ticks += 1
                    if same_action_ticks > same_action_ticks_limit:
                        feedback -= same_action_feedback_exp ** (same_action_ticks - same_action_ticks_limit) - 1

                feedback = math.tanh(feedback / 10) * 10

                connector.write(feedback=feedback, values=data_iterator.__next__())

            elif hasattr(query, 'prefetch'):
                for i in range(int(query.prefetch)):
                    connector.write(values=data_iterator.__next__())

            else:
                raise Exception('Bad message received, ' + query)
        except StopIteration:
            connector.write(stop=True)
        except Exception as e:
            print(e, file=sys.stderr)
            raise
            # continue  # Hopefully, it would all work fine

    with open('result.txt', mode='w') as results_file:
        results_file.write(str(fitness) + '\n')


if __name__ == '__main__':
    __main__()

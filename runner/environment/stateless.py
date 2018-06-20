from os import path
import itertools
import math
import pandas

from .spaces import TupleSpace, BoxSpace, SetSpace
from .signal_reader import SignalReader
from ..preprocessor import RawPreprocessor


def collapse_single_item(li):
    if len(li) == 1:
        return li[0]
    else:
        return li


class StatelessMode():
    def __init__(self, name, from_date, to_date, steps):
        self.name = name
        self.from_date = from_date
        self.to_date = to_date
        self.steps = steps
        self.last_result = None


class Portfolio():  # TODO: Commisions?
    def __init__(self, starting_balance, starting_asset=0.0):
        self.balance = starting_balance
        self.asset = starting_asset
        self.last_operation_price = 0

    def sell(self, price):
        if self.asset != 0:
            self.balance = self.balance + price * self.asset
            self.asset = 0
            self.last_operation_price = price

    def buy(self, price):
        if self.balance != 0:
            self.asset = self.asset + self.balance / price
            self.balance = 0
            self.last_operation_price = price

    def get_total_balance(self, price=None):
        return self.balance + self.asset * (price or self.last_operation_price)


class StatelessEnv():
    def __init__(self, inputs, signals_base_path, interactive, trades_output=None, *args, **kwargs):
        self.inputs = inputs
        self.trades_output = trades_output

        self.action_space = SetSpace([0, 1])
        self.observation_space = None

        self.same_action_ticks_limit = 500
        self.same_action_feedback_exp = 1.005

        self.mode = None
        self.modes = {}

        self.signals = None
        self.signals_iterator = None
        self.next_item = None

        self.start_price = None
        self.observed_min_price = None
        self.observed_max_price = None
        self.observed_min_before_max = False
        self.last_action = 0
        self.same_action_ticks = 0
        self.interactive = interactive
        self.starting_currency = 1000.0
        self.portfolio = None

        self.debug_i = 0
        self.collect_data(signals_base_path, [
            ('learning', None),
            ('test', '180 d'),
            ('validation', '180 d'),
        ])
        self.set_mode('learning')

    # def state_generator(self):
    #     while True:
    #         yield (pandas.Timestamp.now(), self.same_action_ticks / 100, self.last_action)

    # modes is list of tuple(name, timespan); timespan might be string/timedelta (absolute) or int/float/None (relative to left data)
    def collect_data(self, signal_base_dir, modes):
        self.signals = []
        self.preprocessors = [RawPreprocessor()] + [preprocessor for preprocessor, source in self.inputs]

        filename = path.join(signal_base_dir, 'cryptocompare_price_data.json')

        self.signals.append(SignalReader(filename, ['close']))

        for preprocessor, source in self.inputs:
            result = None  # repositories[source["from"]][source["name"]]
            if source['from'] == 'local':
                name = source['name'].split('.')
                if name[0] == 'market':
                    if name[1] == 'all':
                        result = SignalReader(filename, ['close', 'high', 'low', 'open', 'volumefrom', 'volumeto'])
                    else:
                        result = SignalReader(filename, [name[1]])
                # if name[0] == 'state':
                #    result = self.state_generator

            if result is None:
                raise NotImplementedError('Unsupported source config: ' + str(source))

            self.signals.append(result)

        from_date = max(signals.from_date for signals in self.signals)
        to_date = min(signals.to_date for signals in self.signals)
        granularity = min(signals.granularity for signals in self.signals)

        auto_timespan_count = 0
        timespan_relative = to_date - from_date
        for mode, timespan in modes:
            if isinstance(timespan, (str, pandas.Timedelta)):
                timespan_relative -= pandas.Timedelta(timespan)
            else:
                auto_timespan_count += timespan or 1

        date_reached = from_date
        for mode, timespan in modes:
            if isinstance(timespan, (str, pandas.Timedelta)):
                timespan = pandas.Timedelta(timespan)
            else:
                timespan = timespan_relative / auto_timespan_count * (timespan or 1)

            self.modes[mode] = StatelessMode(mode, date_reached, date_reached + timespan, timespan / granularity)
            date_reached += timespan

        spaces = []
        for preprocessor, signal in zip(self.preprocessors, self.signals[1:]):
            if preprocessor.output_single_rows:
                spaces.append(BoxSpace(-100, 100, signal.shape))
            else:
                spaces.append(BoxSpace(-100, 100, (preprocessor.output_window_length,) + signal.shape))

        self.observation_space = TupleSpace(spaces)

        print('Loaded Data:', ', '.join('{days} {name} days'.format(
            name=mode.name,
            days=(mode.to_date - mode.from_date) // '1d'
        ) for mode in self.modes.values()))

    def set_mode(self, mode):
        self.mode = self.modes[mode]

    def _get_next_observation(self):
        if self.next_item is None:
            try:
                self.next_item = self.signals_iterator.__next__()
            except StopIteration:
                print('No data provided')
                raise

        observation = [preprocessor.append_and_preprocess(input_row[1:]) for preprocessor, input_row in zip(self.preprocessors, self.next_item)]

        date = self.next_item[0][0]

        try:
            self.next_item = self.signals_iterator.__next__()
        except StopIteration:
            self.next_item = None
            return (observation, date, True)
        return (observation, date, False)

    def reset(self):
        self.signals_iterator = zip(*[iter(signal.get_iterator(self.mode.from_date, self.mode.to_date)) for signal in self.signals])
        self.debug_i = 0
        self.last_action = 0
        self.next_item = None
        self.observed_min_price = None
        self.observed_max_price = None
        self.observed_min_before_max = False
        self.same_action_ticks = 0
        self.portfolio = Portfolio(self.starting_currency)

        for preprocessor, signal in zip(self.preprocessors, self.signals[1:]):
            preprocessor.reset_state()
            preprocessor.init((thing[1:] for thing in itertools.islice(signal.get_iterator(self.mode.from_date, self.mode.to_date), preprocessor.prefetch_tick_count)))

        print('Started {mode.name} episode ({mode.from_date})'.format(mode=self.mode))
        if self.trades_output and self.mode.name != 'learning':
            print('', file=self.trades_output)
            print('{mode}\t{from_date}\t{to_date}'.format(
                mode=self.mode.name,
                from_date=pandas.to_datetime(self.mode.from_date).strftime('%Y-%m-%dT%H:%MZ'),
                to_date=pandas.to_datetime(self.mode.to_date).strftime('%Y-%m-%dT%H:%MZ')
            ), file=self.trades_output)
            print('action\tdate\tprice\tbalance', file=self.trades_output)

        observation, date, is_final = self._get_next_observation()

        self.start_price = observation[0][0]

        return collapse_single_item(observation[1:])

    def step(self, action):
        self.debug_i += 1

        observation, date, is_final = self._get_next_observation()
        price = observation[0][0]

        if self.debug_i % 25 == 0 and self.interactive:
            print(' Step {step: =6} ({date}) ${balance: >7.2f} {state}     \r'.format(
                step=self.debug_i,
                date=date,
                balance=self.portfolio.get_total_balance(price),
                state='bougth' if action == 1 else 'sold  '  # Spaces are important, both need to be same length
            ), end='')

        if self.observed_min_price is None or self.observed_min_price > price:
            self.observed_min_price = price
            self.observed_min_before_max = False
        if self.observed_max_price is None or self.observed_max_price < price:
            self.observed_max_price = price
            self.observed_min_before_max = True

        feedback = 0.0

        if action != self.last_action:
            if self.trades_output and self.mode.name != 'learning':
                print('{action}\t{date}\t{price}\t{balance}'.format(
                    date=pandas.to_datetime(date).strftime('%Y-%m-%dT%H:%MZ'),
                    price=price,
                    action='Buy' if action == 1 else 'Sell',
                    balance=self.portfolio.get_total_balance(price),
                ), file=self.trades_output)

            self.last_action = action
            self.same_action_ticks = 0

            if action == 1:
                self.portfolio.buy(price)

            elif action == 0:
                feedback += price - self.portfolio.last_operation_price
                self.portfolio.sell(price)
        else:
            self.same_action_ticks += 1
            if self.same_action_ticks > self.same_action_ticks_limit:
                feedback -= self.same_action_feedback_exp ** (self.same_action_ticks - self.same_action_ticks_limit) - 1

        feedback = math.tanh(feedback / 10) * 10

        if is_final:
            self.finish_episode(price)

        return (collapse_single_item(observation[1:]), feedback, is_final, {})

    def finish_episode(self, end_price):
        if self.signals_iterator is None:
            return

        self.signals_iterator = None

        baseline_portfolio = Portfolio(self.starting_currency)
        if False:  # Just buy/sell at the ends, not a good comparision as the rest
            baseline_portfolio.buy(self.start_price)
            baseline_portfolio.sell(end_price)
        elif self.observed_min_before_max:
            baseline_portfolio.buy(self.observed_min_price)
            baseline_portfolio.sell(self.observed_max_price)
        else:
            baseline_portfolio.buy(self.start_price)
            baseline_portfolio.sell(self.observed_max_price)
            baseline_portfolio.buy(self.observed_min_price)
            baseline_portfolio.sell(end_price)

        baseline_result = baseline_portfolio.get_total_balance(end_price) - self.starting_currency
        agent_result = self.portfolio.get_total_balance(end_price) - self.starting_currency
        result = (agent_result / baseline_result) * 100

        self.mode.last_result = result
        print('')  # Makes a newline on stderr
        print('{mode} episode score: {result: >+4.2f} (agent: {agent: >+8.2f}, baseline: {baseline: >+8.2f})'.format(
            mode=self.mode.name.capitalize(),
            result=result,
            agent=agent_result,
            baseline=baseline_result
        ))

    def close(self):
        self.signals_iterator = None

    def seed(self, seed=None):
        pass

    def configure(self, *args, **kwargs):
        pass

import math
import time

from .spaces import TupleSpace, BoxSpace, SetSpace
from .signal_combiner import SignalCombiner
from ..preprocessor import RawPreprocessor


hour = 60 * 60
day = hour * 24
year = day * 365


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
            new_balance = self.balance + price * self.asset
            commission = self.calculate_commission(new_balance)
            self.balance = new_balance - commission
            self.asset = 0
            self.last_operation_price = price

    def buy(self, price):
        if self.balance != 0:
            new_asset = self.asset + self.balance / price
            commission = self.calculate_commission(new_asset)
            self.asset = new_asset - commission
            self.balance = 0
            self.last_operation_price = price

    def get_total_balance(self, price=None):
        return self.balance + self.asset * (price or self.last_operation_price)

    def calculate_commission(self, price):
        return price * 0.002


class StatelessEnv():
    def __init__(self, inputs, repositories, interactive, trades_output=None, *args, **kwargs):
        self.trades_output = trades_output
        self.interactive = interactive

        self.action_space = SetSpace([0, 1])
        self.observation_space = None

        self.same_action_ticks_limit = 500
        self.same_action_feedback_exp = 1.005

        self.mode = None
        self.modes = {}

        self.combined_signal = None
        self.next_item = None

        self.start_price = None
        self.observed_min_price = None
        self.observed_max_price = None
        self.observed_min_before_max = False
        self.last_action = 0
        self.same_action_ticks = 0
        self.starting_currency = 1000.0
        self.portfolio = None

        self.debug_i = 0

        self.collect_data(repositories, inputs, [
            ('learning', None),
            ('validation', year // 2),
            ('test', year // 2),
        ])
        self.set_mode('learning')

    # def state_generator(self):
    #     while True:
    #         yield (pandas.Timestamp.now(), self.same_action_ticks / 100, self.last_action)

    def collect_data(self, repositories, inputs, modes):
        signals = []

        signals.append((repositories['cryptocompare']['market'], 'close', RawPreprocessor()))  # 'close'

        for preprocessor, source in inputs:
            if source["from"] in repositories and source["name"] in repositories[source["from"]]:
                signals.append((
                    repositories[source["from"]][source["name"]],
                    source.get("component", None),
                    preprocessor))
            else:
                raise NotImplementedError('Unsupported source config: ' + str(source))

        self.combined_signal = SignalCombiner(signals)

        auto_timespan_count = 0
        auto_timespan = self.combined_signal.available_to - self.combined_signal.available_from
        for mode, timespan in modes:
            if timespan is None:
                auto_timespan_count += 1
            else:
                auto_timespan -= timespan

        date_reached = self.combined_signal.available_from
        for mode, timespan in modes:
            if timespan is None:
                timespan = auto_timespan // auto_timespan_count

            self.modes[mode] = StatelessMode(mode, date_reached, date_reached + timespan, timespan / self.combined_signal.granularity)
            date_reached += timespan

        spaces = []
        for signal, component, preprocessor in signals:
            shape = signal.shape
            if component is not None:
                shape = shape[1:]
            if not preprocessor.output_single_rows:
                shape = (preprocessor.output_window_length,) + shape
            spaces.append(BoxSpace(-100, 100, shape))

        self.observation_space = TupleSpace(spaces)

        print('Loaded Data:', ', '.join('{days} {name} days'.format(
            name=mode.name,
            days=(mode.to_date - mode.from_date) // day
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

        observation = self.next_item[1]
        date = self.next_item[0]

        try:
            self.next_item = self.signals_iterator.__next__()
        except StopIteration:
            self.next_item = None
            return (observation, date, True)
        return (observation, date, False)

    def reset(self):
        self.signals_iterator = iter(self.combined_signal.iterate(self.mode.from_date, self.mode.to_date))
        self.debug_i = 0
        self.last_action = 0
        self.next_item = None
        self.observed_min_price = None
        self.observed_max_price = None
        self.observed_min_before_max = False
        self.same_action_ticks = 0
        self.portfolio = Portfolio(self.starting_currency)

        print('Started {mode.name} episode ({mode.from_date})'.format(mode=self.mode))
        if self.trades_output and self.mode.name != 'learning':
            print('', file=self.trades_output)
            print('{mode}\t{from_date}\t{to_date}'.format(
                mode=self.mode.name,
                from_date=time.strftime('%Y-%m-%dT%H:%MZ', time.gmtime(self.mode.from_date)),
                to_date=time.strftime('%Y-%m-%dT%H:%MZ', time.gmtime(self.mode.to_date))
            ), file=self.trades_output)
            print('action\tdate\tprice\tbalance', file=self.trades_output)

        observation, date, is_final = self._get_next_observation()

        self.start_price = observation[0]

        return collapse_single_item(observation[1:])

    def step(self, action):
        self.debug_i += 1

        observation, date, is_final = self._get_next_observation()
        price = observation[0]

        if self.debug_i % 25 == 0 and self.interactive:
            print(' Step {step: =6} ({date}) ${balance: >7.2f} {state}     \r'.format(
                step=self.debug_i,
                date=time.strftime('%Y-%m-%dT%H:%MZ', time.gmtime(date)),
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
                    date=time.strftime('%Y-%m-%dT%H:%MZ', time.gmtime(date)),
                    price=price,
                    action='Buy' if action == 1 else 'Sell',
                    balance=self.portfolio.get_total_balance(price),
                ), file=self.trades_output)

            self.last_action = action
            self.same_action_ticks = 0

            if action == 1:
                self.portfolio.buy(price)

            elif action == 0:
                feedback += price - self.portfolio.last_operation_price \
                            - self.portfolio.calculate_commission(price) \
                            - self.portfolio.calculate_commission(self.portfolio.last_operation_price)
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

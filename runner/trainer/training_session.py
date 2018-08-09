from .portfolio import FixedTransactionPortfolio
import logging

logger = logging.getLogger('trainer')


class TrainingSession:
    def __init__(self, trainer, episode, trades_output=None):
        logger.info('Started {episode} session'.format(episode=episode))
        (price_iterator, observation_iterator) = trainer.get_episode_iterators(episode)
        self.iterator = zip(price_iterator, observation_iterator)
        self.current_item = self.iterator.__next__()
        self.next_item = self.iterator.__next__()
        self.last_feedback = 0.0
        self.give_feedback_on = 'sell'
        self.give_feedback_on_new_action = True
        self.last_feedback_total = 0.0
        self.last_price = 0.0
        self.last_action = 'none'
        self.portfolio = FixedTransactionPortfolio(100.0)
        self.trades_output = trades_output
        self.trainer = trainer
        if self.trades_output is not None:
            print('', file=self.trades_output)
            print('action\tdate\tprice\tbalance\tassets', file=self.trades_output)

    def get_price(self):
        return self.current_item[0][1]

    def get_date(self):
        return self.current_item[0][0]

    def get_observation(self):
        return self.current_item[1]

    def is_observation_final(self):
        return self.next_item is None

    def get_feedback(self):
        return self.last_feedback

    def give_action(self, action):
        if action == 'buy':
            self.portfolio.buy(self.get_price())
        elif action == 'sell':
            self.portfolio.sell(self.get_price())

        self.last_feedback = 0.0
        if self.last_action != action or not self.give_feedback_on_new_action:
            if action == self.give_feedback_on or self.give_feedback_on == 'any':
                feedback_total = self.portfolio.get_total_balance(self.get_price())

                if self.last_feedback_total != 0.0:
                    self.last_feedback = feedback_total - self.last_feedback_total

                self.last_feedback_total = feedback_total
        self.last_action = action
        self.last_price = self.get_price()
        self.current_item = self.next_item
        try:
            self.next_item = self.iterator.__next__()
        except StopIteration:
            self.next_item = None

        if self.trades_output and action in ['buy', 'sell']:
            print('{action}\t{date}\t{price}\t{balance}\t{assets}'.format(
                action=action,
                date=self.get_date(),
                price=self.get_price(),
                balance=self.portfolio.balance,
                assets=self.portfolio.assets,
            ), file=self.trades_output)

    def get_result(self):
        return self.portfolio.get_total_balance(self.last_price)

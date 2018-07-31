class Portfolio:
    def __init__(self, starting_balance, starting_asset=0.0, commission=0.002):
        self.balance = starting_balance
        self.assets = starting_asset
        self.commission = commission

        self.last_transaction_price = 0

    def sell(self, price):
        raise NotImplementedError()

    def buy(self, price):
        raise NotImplementedError()

    def get_total_balance(self, price):
        return self.balance + self.assets * (price or self.last_transaction_price)

    def calculate_commission(self, transaction_balance):
        return transaction_balance * self.commission


class FixedTransactionPortfolio(Portfolio):
    def __init__(self, transaction_amount, starting_balance=0.0, *args):
        super().__init__(starting_balance, *args)
        self.transaction_amount = transaction_amount

    def sell(self, price):
        if self.assets > 0:
            revenue = self.assets * price
            commission = self.calculate_commission(revenue)

            self.assets = 0
            self.balance = self.balance + revenue - commission

            self.last_transaction_price = price

    def buy(self, price):
        if self.assets < self.transaction_amount:
            cost = price * (self.transaction_amount - self.assets)
            commission = self.calculate_commission(cost)

            self.balance = self.balance - cost - commission
            self.assets = self.transaction_amount

            self.last_transaction_price = price

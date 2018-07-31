class Signal:
    def __init__(self, shape, granularity, available_from, available_to=-1):
        self.shape = shape
        self.granularity = granularity
        self.available_from = available_from
        self.available_to = available_to

    def iterate(self, from_date, to_date):
        raise NotImplementedError()

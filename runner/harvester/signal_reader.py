class SignalReader:
    def __init__(self, name, shape, granularity, available_from, available_to):
        self.name = name
        self.shape = shape
        self.granularity = granularity
        self.available_from = available_from
        self.available_to = available_to

    def iterate(self, from_date, to_date):
        return NotImplementedError()


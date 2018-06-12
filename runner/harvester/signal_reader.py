class SignalReader:
    def __init__(self, name, shape, components, granularity, available_from, available_to):
        self.name = name
        self.shape = shape
        self.components = components
        self.granularity = granularity
        self.available_from = available_from
        self.available_to = available_to

    def iterate(self, from_date, to_date):
        return NotImplementedError()

    def iterate_components(self, component, from_date, to_date):
        print('iterate_components')
        # returns component

    # data should be cached

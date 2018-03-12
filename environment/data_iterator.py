import pandas
import numpy


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

import os
import pandas
import functools


@functools.lru_cache()
def read_file(filename, format):
    if format == 'csv':
        return pandas.read_csv(
            filename,
            index_col=0,
            parse_dates=True).tz_localize('UTC')
    elif format == 'hdf':
        return pandas.read_hdf(filename).tz_localize('UTC')
    elif format == 'json':
        result = pandas.read_json(filename, orient='records')
        result.index = pandas.to_datetime(list(result.time), unit='s').tz_localize('UTC')
        return result


class SignalReader(object):
    def __init__(self, filename, columns, format=None):
        super(object, self).__init__()

        if format is None:
            format = os.path.splitext(os.path.basename(filename))[1][1:]
        self.data = read_file(filename, format)[columns]

        self.granularity = self.data.index[1] - self.data.index[0]
        self.from_date = self.data.index[0]
        self.to_date = self.data.index[-1]
        self.shape = (len(columns),)

    def get_iterator(self, from_date, to_date):
        from_row = (from_date - self.from_date) // self.granularity
        to_row = (to_date - self.from_date) // self.granularity
        return self.data.iloc[from_row:to_row].itertuples()

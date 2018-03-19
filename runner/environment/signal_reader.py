import os
import pandas


class SignalReader(object):
    def __init__(self, filename, column=None, target_column=None, format=None):
        super(object, self).__init__()

        self.filename = filename
        if column is not None:
            self.column = column
        else:
            self.column = os.path.splitext(os.path.basename(filename))[0]
        self.target_column = self.column if target_column is None else target_column
        self.format = os.path.splitext(os.path.basename(filename))[1][1:] if format is None else format

    def read_all(self):
        if self.format == 'csv':
            return (pandas.read_csv(
                    self.filename,
                    index_col=0,
                    parse_dates=True)

                    .tz_localize('UTC')
                    .rename(columns={self.column: self.target_column})).tail(2000)  # HACK
        elif self.format == 'hdf':
            return pandas.read_hdf(self.filename).tz_localize('UTC')
        elif self.format == 'json':
            result = pandas.read_json(self.filename, orient='records')
            result.index = pandas.to_datetime(list(result.time), unit='s').tz_localize('UTC')
            return result

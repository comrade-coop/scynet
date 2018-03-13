import os
import pandas


class SignalReader(object):
    def __init__(self, filename, column=None, target_column=None, chunk_size=2**8):
        super(object, self).__init__()

        self.filename = filename
        if column is not None:
            self.column = column
        else:
            self.column = os.path.splitext(os.path.basename(filename))[0]
        self.target_column = self.column if target_column is None else target_column

    def read_all(self):
        return (pandas.read_csv(
                self.filename,
                index_col=0,
                parse_dates=True)

                .tz_localize('UTC')
                .rename(columns={self.column: self.target_column})).tail(2000)  # HACK
        # return pandas.read_hdf(self.filename).tz_localize('UTC')

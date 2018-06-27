from .signal_reader import SignalReader
import requests
import pandas as pd
import csv
import time


class DataServerSignalReader(SignalReader):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.date_index = None
        self.price_index = None

    def _iterate(self, from_time, to_time):
        uri = self.config['server_url'] + '/?method=read&provider=blockchain&id=' + self.name + '&form=csv'

        current_from_time = from_time
        next_to_time = current_from_time + self.config['limit'] * self.granularity
        current_to_time = next_to_time if (next_to_time < to_time) else to_time

        while current_to_time <= to_time:
            url_start_end = uri + '&start=' + str(pd.to_datetime(current_from_time, unit='s')) + '&end=' + str(
                pd.to_datetime(current_to_time, unit='s'))

            response = requests.get(url_start_end, stream=True)

            if response.ok:
                for chunk in response.iter_content(chunk_size=None):
                    if not chunk:
                        break

                    # create list from csv data
                    decoded_chunk_data = chunk.decode('utf-8')
                    cr = csv.reader(decoded_chunk_data.splitlines(), delimiter=',')
                    ticks = list(cr)

                    if self.date_index is None or self.price_index is None:
                        self.date_index = ticks[0].index('date')
                        self.price_index = ticks[0].index(self.name)

                    # oldest to newest
                    for i in range(1, len(ticks)):
                        row = ticks[i]
                        yield (self._time_to_seconds(row[self.date_index]), row[self.price_index])

                # current_to_time should be next_to_time also when it is equal to to_time
                # in order to be able to break the while
                current_from_time = self._time_to_seconds(ticks[len(ticks) - 1][self.date_index]) + self.granularity
                next_to_time = current_from_time + self.config['limit'] * self.granularity
                current_to_time = next_to_time if (current_to_time == to_time or next_to_time < to_time) else to_time

    def _time_to_seconds(self, time_to_convert):
        dt = time.strptime(time_to_convert, '%Y-%m-%d %H:%M:%S')
        sec = int(time.mktime(dt) - time.timezone)
        return sec

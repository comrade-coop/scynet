from .signal_reader import SignalReader
import requests
import pandas as pd
import csv
import time


class DataServerSignalReader(SignalReader):
    def __init__(self, name, **kwargs):
        super().__init__(name, **kwargs)

    def _iterate(self, from_time, to_time):
        # call server here
        uri = 'http://127.0.0.1:8000/?method=read&provider=blockchain&id=' + self.name + '&form=csv' \
               '&start=' + str(pd.to_datetime(from_time, unit='s')) \
              + '&end=' + str(pd.to_datetime(to_time, unit='s'))

        response = requests.get(uri, stream=True)

        decoded_content = response.content.decode('utf-8')
        cr = csv.reader(decoded_content.splitlines(), delimiter=',')
        my_list = list(cr)
        for i in range(1, len(my_list)):
            row = my_list[i]
            dt = time.strptime(row[2], '%Y-%m-%d %H:%M:%S')
            sec = int(time.mktime(dt) - time.timezone)
            yield (sec, row[1])

from .signal_reader import SignalReader
import requests
import json
import pandas as pd


class CryptocompareSignalReader(SignalReader):
    def __init__(self, name, **kwargs):
        super().__init__(name, **kwargs)

    def _iterate(self, from_time, to_time):
        uri = 'https://min-api.cryptocompare.com/data/histohour?fsym=ETH&tsym=USD&limit=10&e=CCCAGG&toTs='+str(to_time)
        response = requests.get(uri, stream=True)

        # print()
        # print('from_time', pd.to_datetime(from_time, unit='s'))
        # print('to_time', pd.to_datetime(to_time, unit='s'))

        if response.ok:
            for chunk in response.iter_content(chunk_size=None):
                if not chunk:
                    break

                chunk_data = json.loads(chunk)['Data']
                oldest_time_in_chunk = chunk_data[0]['time']
                # print('oldest_time_in_chunk', pd.to_datetime(oldest_time_in_chunk, unit='s'))

                # newest to oldest
                for json_tick in reversed(chunk_data):
                    if json_tick['time'] < from_time:
                        break

                    yield self._create_tuple(json_tick)

                if oldest_time_in_chunk >= from_time:
                    for tuple_tick in self._iterate(from_time, oldest_time_in_chunk - self.granularity):
                        yield tuple_tick

    def _create_tuple(self, tick):
        components_list = []
        for component in self.components:
            components_list.append(tick[component])

        tick_tuple = (tick['time'], components_list)
        return tick_tuple

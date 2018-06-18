from .signal_reader import SignalReader
import requests
import json
import pandas as pd


class CryptocompareSignalReader(SignalReader):
    def __init__(self, name, **kwargs):
        super().__init__(name, **kwargs)

    def _iterate(self, from_time, to_time):
        limit = 10

        uri = 'https://min-api.cryptocompare.com/data/histohour?fsym=ETH&tsym=USD&limit=' + str(limit) + '&e=CCCAGG'
        to_ts = '&toTs=' + str(from_time + limit*self.granularity)
        uri = uri + to_ts
        response = requests.get(uri, stream=True)

        if response.ok:
            for chunk in response.iter_content(chunk_size=None):
                if not chunk:
                    break

                chunk_data = json.loads(chunk)['Data']
                newest_time_in_chunk = chunk_data[len(chunk_data)-1]['time']

                # oldest to newest
                for json_tick in chunk_data:
                    if json_tick['time'] > to_time:
                        break

                    yield self._create_tuple(json_tick)

                if newest_time_in_chunk < to_time:
                    for tuple_tick in self._iterate(newest_time_in_chunk + self.granularity, to_time):
                        yield tuple_tick

    def _create_tuple(self, tick):
        components_list = []
        for component in self.components:
            components_list.append(tick[component])

        tick_tuple = (tick['time'], components_list)
        return tick_tuple

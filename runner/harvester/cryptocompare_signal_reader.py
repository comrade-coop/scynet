from .signal_reader import SignalReader
import requests
import json


class CryptocompareSignalReader(SignalReader):
    def __init__(self, name, **kwargs):
        super().__init__(name, **kwargs)

    def _iterate(self, from_time, to_time):
        uri = 'https://min-api.cryptocompare.com/data/histohour?fsym=ETH&tsym=USD&limit='+str(self.limit)+'&e=CCCAGG'

        current_from_time = from_time
        while current_from_time < to_time:
            to_ts = '&toTs=' + str(current_from_time + (self.limit * self.granularity))
            uri_tots = uri + to_ts
            response = requests.get(uri_tots, stream=True)

            if response.ok:
                for chunk in response.iter_content(chunk_size=None):
                    if not chunk:
                        break

                    chunk_data = json.loads(chunk)['Data']

                    # oldest to newest
                    for json_tick in chunk_data:
                        if json_tick['time'] > to_time:
                            break

                        yield self._create_tuple(json_tick)

                    # current_from_time is the newest_time_in_chunk
                    current_from_time = chunk_data[len(chunk_data) - 1]['time']

    def _create_tuple(self, tick):
        components_list = []
        for component in self.components:
            components_list.append(tick[component])

        tick_tuple = (tick['time'], components_list)
        return tick_tuple

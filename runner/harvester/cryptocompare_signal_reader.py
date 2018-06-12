from .signal_reader import SignalReader
import requests
import json


class CryptocompareSignalReader(SignalReader):
    def __init__(self, name, **kwargs):
        super().__init__(name, **kwargs)

    def iterate(self, from_date, to_date):
        uri = 'https://min-api.cryptocompare.com/data/histohour?fsym=ETH&tsym=USD&limit=5&e=CCCAGG&toTs='+str(to_date)
        response = requests.get(uri, stream=True)

        if response.ok:
            for chunk in response.iter_content(chunk_size=None):
                if not chunk:
                    break

                chunk_data = json.loads(chunk)['Data']
                start_date = chunk_data[0]['time']

                for item in reversed(chunk_data):
                    if item['time'] < from_date:
                        break

                    components_list = []
                    for component in self.components:
                        components_list.append(item[component])

                    chunk_tuple = (item['time'], components_list)
                    yield chunk_tuple

                if start_date >= from_date:
                    for x in self.iterate(from_date, start_date - 1):
                        yield x

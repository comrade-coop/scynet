from .signal_reader import SignalReader
import requests


class CryptocompareSignalReader(SignalReader):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

    def _iterate(self, from_time, to_time):
        uri = 'https://min-api.cryptocompare.com/data/histohour?fsym=ETH&tsym=USD'

        current_from_time = from_time
        while current_from_time < to_time:
            response = requests.get(uri, params={
                'toTs': min(current_from_time + self.config['limit'] * self.granularity, to_time),
                'limit': min(self.config['limit'], to_time - current_from_time),
            })

            data = response.json()['Data']

            for tick in data:
                if tick['time'] > to_time:
                    break

                yield (tick['time'], [tick[component] for component in self.components])

            current_from_time = data[-1]['time']

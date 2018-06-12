from .signal_reader import SignalReader
import requests


class DataServerSignalReader(SignalReader):
    def __init__(self, name, **kwargs):
        super().__init__(name, **kwargs)

    def iterate(self, from_date, to_date):
        # call server here
        print("call server")
        uri = 'http://127.0.0.1:8000/?method=list&provider=blockchain'
        response = requests.get(uri, stream=True)

        if response.ok:
            for chunk in response.iter_content(chunk_size=None):
                if not chunk:
                    break
                yield chunk

import requests
import json
import time


def main():
    blockchain_properties = get_blockchain_properties()
    cryptocompare_properties = get_cryptocompare_properties()

    repositories = {
        "cryptocompare": cryptocompare_properties,
        "blockchain": blockchain_properties
    }

    with open('repositories.json', 'w') as repos_file:
        json.dump(repositories, repos_file, separators=(',', ':'), indent=4)


def get_blockchain_properties():
    blockchain_data = {}
    uri = "http://localhost:8000/?method=list&provider=blockchain"
    response = requests.get(uri)
    if response.ok:
        properties = json.loads(response.text)
        for property in properties:
            uri = "http://localhost:8000/?method=metadata&provider=blockchain&id=" + property
            metadata_response = requests.get(uri)
            if metadata_response.ok:
                metadata = json.loads(metadata_response.text)
                blockchain_data[property] = {
                    "shape": metadata['shape'],
                    "granularity": metadata['frequency'],
                    "available_from": _time_to_seconds(metadata['start']),
                    "available_to": _time_to_seconds(metadata['end']),
                    "type": "dataserver",
                    "config": {
                        "server_url": "http://127.0.0.1:8000",
                        "limit": 1000
                    }
                }
    return blockchain_data


def get_cryptocompare_properties():
    market_data = {
        "market": {
            "shape": [
                6
            ],
            "components": [
                "close",
                "high",
                "low",
                "open",
                "volumefrom",
                "volumeto"
            ],
            "granularity": 3600,
            "available_from": 1451606400,
            "available_to": 1530144000,
            "type": "cryptocompare",
            "config": {
                "limit": 1000
            }
        }
    }

    return market_data


def _time_to_seconds(time_to_convert):
    dt = time.strptime(time_to_convert, '%Y-%m-%d %H:%M:%S')
    sec = int(time.mktime(dt) - time.timezone)
    return sec


if __name__ == '__main__':
    main()

import json
import argparse

from .data_server_signal_reader import DataServerSignalReader

class_map = {
    'dataserver': DataServerSignalReader
}


def load_json(filename):
    with open(filename) as f:
        return json.load(f)


def parse_repository(reader_type, name, config):
    reader_class = class_map[reader_type]

    if reader_class is None:
        raise Exception("Invalid reader class name %s." % name)

    return reader_class(name, **config)


def parse_repositories(filename):
    signals_map = {}
    data = load_json(filename)
    for data_source in data:
        item = data[data_source]
        signals_map[data_source] = {}
        for prop in item:
            if prop not in signals_map:
                signal_class = parse_repository(item[prop]['type'], prop, item[prop]['config'])
                signals_map[data_source][prop] = signal_class

    return signals_map


def init():
    parser = argparse.ArgumentParser(description="harvester")
    parser.add_argument('input', help='The JSON file input.')
    args, _ = parser.parse_known_args()
    filename = args.input

    readers = parse_repositories(filename)

    for provider in readers:
        item = readers[provider]
        print()
        for prop in item:
            signal_reader = item[prop]
            print("Reader: ", signal_reader)
            for x in signal_reader.iterate(5, 6):
                print("X: ", x)


if __name__ == "__main__":
    init()

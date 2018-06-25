import json
import argparse
import pandas as pd

from .data_server_signal_reader import DataServerSignalReader
from .cryptocompare_signal_reader import CryptocompareSignalReader

class_map = {
    'dataserver': DataServerSignalReader,
    'cryptocompare': CryptocompareSignalReader
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


def print_tick_tuple(tick):
    dt = pd.to_datetime(tick[0], unit='s')
    tuple = (dt, tick[1])
    print(tuple)


def test_iterate(readers):
    for provider in readers:
        item = readers[provider]
        print()
        for prop in item:
            signal_reader = item[prop]
            print("Reader: ", signal_reader)

            '''for x in signal_reader.iterate(1454284800, 1454389200):
                print_tick_tuple(x)

            print()
            print("NEXT 2")
            print()

            for x in signal_reader.iterate(1454407200, 1454432400):
                print_tick_tuple(x)

            print()
            print("NEXT 3")
            print()'''

            for x in signal_reader.iterate(1454277600, 1454450400):
                print_tick_tuple(x)


def test_iterate_component(readers, component):
    for provider in readers:
        item = readers[provider]
        print()
        for prop in item:
            signal_reader = item[prop]
            print("Reader: ", signal_reader)

            for x in signal_reader.iterate_component(component, 1528826400, 1528999200):
                print(x)


def init():
    parser = argparse.ArgumentParser(description="harvester")
    parser.add_argument('input', help='The JSON file input.')
    args, _ = parser.parse_known_args()
    filename = args.input

    readers = parse_repositories(filename)
    test_iterate(readers)
    # test_iterate_component(readers, 'high')


if __name__ == "__main__":
    init()

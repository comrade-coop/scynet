import os
import argparse
import time
from . import parse_repositories


def test_iterate(readers):
    def print_tick_tuple(tick):
        print(time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime(tick[0])), tick[1])
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
    parser.add_argument('--input', help='The JSON file input.')
    parser.add_argument('--test', help='Run tests.')
    args, _ = parser.parse_known_args()

    repositories = parse_repositories(args.input or os.path.join(os.path.dirname(__file__), '../../repositories.json'))

    if args.test:
        test_iterate(repositories)
        # test_iterate_component(repositories, 'high')
    else:
        try:
            os.mkdir(os.path.join(os.path.dirname(__file__), '../signals'))
        except FileExistsError:
            pass
        for source_name, source in repositories.items():
            for reader_name, reader in source.items():
                print('Starting reader %s' % reader_name)
                reader.create_cache()
                print('Reader %s finished' % reader_name)


init()

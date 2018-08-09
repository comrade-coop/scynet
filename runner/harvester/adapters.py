import json
import collections
from .adapter.cryptocompare_adapter import CryptocompareSignalAdapter
from .adapter.data_server_adapter import DataServerSignalAdapter


class_map = {
    'dataserver': DataServerSignalAdapter,
    'cryptocompare': CryptocompareSignalAdapter,
}
adapters = collections.defaultdict(lambda: {})


def parse_repository(name, config):
    reader_class = class_map[config['type']]

    if reader_class is None:
        raise Exception("Invalid reader class name for %s." % name)

    return reader_class(name, **config)


def parse_repositories(filename):
    with open(filename) as f:
        data = json.load(f)

    for data_source in data:
        for prop in data[data_source]:
            adapters[data_source][prop] = parse_repository(prop, data[data_source][prop])

    return adapters

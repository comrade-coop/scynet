from .adapters import parse_repositories
from .signal import Signal
from .signal_combiner import SignalCombiner

from .signal_reader import SignalReader
from .preprocessor.talib_signal_reader import TalibSignalReader
from .preprocessor.raw_preprocessor import RawPreprocessor
from .preprocessor.mean_stdev_preprocessor import MeanStdevPreprocessor


class_map = {
    'RawPreprocessor': RawPreprocessor,
    'MeanStdevPreprocessor': MeanStdevPreprocessor,
    'SignalReader': SignalReader,
    'TalibSignalReader': TalibSignalReader,
}


def instance_class(config, **kwargs):
    return class_map[config['type']](**kwargs, **config['config'])


def parse_input_specification(config):
    signal = instance_class(config)
    if 'preprocessor' in config:
        signal = instance_class(config['preprocessor'], signal=signal)
    return signal


__all__ = ['parse_repositories', 'parse_input_specification', 'Signal', 'SignalCombiner']

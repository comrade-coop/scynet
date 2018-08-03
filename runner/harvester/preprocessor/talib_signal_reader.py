import numpy
from talib import abstract
from .preprocessor import Preprocessor
from ..signal_reader import SignalReader


class TalibSignalReader(Preprocessor):
    def __init__(self, source='cryptocompare', name='market', talib_function='SMA', parameters=[], preprocess_window_length=10):
        super().__init__(SignalReader(source, name), preprocess_window_length, windowing_strategy='previous')
        self.talib_function = abstract.Function(talib_function)
        self.shape = (len(self.talib_function.output_names),)
        self.parameters = parameters

    def _preprocess_window(self, window):
        inputs = {
            'close': window[:, 0],
            'high': window[:, 1],
            'low': window[:, 2],
            'open': window[:, 3],
            'volume': window[:, 4]
        }

        outputs = self.talib_function(inputs, len(window) - 1, *self.parameters)
        if not isinstance(outputs, list):
            outputs = [outputs]

        outputs = numpy.asanyarray(outputs).T

        return outputs

import numpy
from talib import abstract
from .preprocessor import Preprocessor
from ..signal_reader import SignalReader


class TalibSignalReader(Preprocessor):
    def __init__(self, source='cryptocompare', name='market', talib_function='SMA'):
        super().__init__(SignalReader(source, name), windowing_strategy='previous')
        self.talib_function = abstract.Function(talib_function)
        self.shape = (len(self.talib_function.output_names),)

    def _preprocess_window(self, window):
        inputs = {
            'close': window[:, 0],
            'high': window[:, 1],
            'low': window[:, 2],
            'open': window[:, 3],
            'volume': window[:, 4]
        }

        outputs = self.talib_function(inputs, len(window))
        if isinstance(outputs, list):
            outputs = numpy.asanyarray(outputs).T

        return outputs

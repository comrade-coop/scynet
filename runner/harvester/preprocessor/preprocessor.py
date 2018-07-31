from collections import deque
import numpy
from ..signal import Signal


class Preprocessor(Signal):
    def __init__(self, signal, preprocess_window_length=1, output_window_length=-1, windowing_strategy='partial', *args, **kwargs):
        """Creates a preprocessor
        Arguments:
            signal - the wrapped signal, of type Signal
            preprocess_window_length - the size of the preprocessing window
            output_window_length - the size of the ouput window. Negative/zero give a single row without a wrapper.
            windowing_strategy - the way the window is used at the start of the period.
                'partial' - progressively grow the window to the wanted size.
                'previous' - request data before the period's start in order to fill the window.
                'first' - use the data in the beginning of the period, giving None while the window is not complete.
        """
        self.signal = signal
        self.output_window_length = min(output_window_length, preprocess_window_length)
        self.preprocess_window_length = preprocess_window_length
        self.windowing_strategy = windowing_strategy

        if self.output_window_length <= 0:
            super().__init__(signal.shape, signal.granularity, signal.available_from, signal.available_to)
        else:
            super().__init__((output_window_length,) + signal.shape, signal.granularity, signal.available_from, signal.available_to)

    def iterate(self, from_date, to_date):
        window = deque(maxlen=self.preprocess_window_length)

        if self.windowing_strategy == 'previous':
            for row in self.signal.iterate(from_date - self.granularity * self.window_size, from_date):
                window.append(row[1:])

        for row in self.signal.iterate(from_date, to_date):
            window.append(row[1])
            if self.windowing_strategy == 'first' or self.windowing_strategy == 'use-previous':
                if len(window) == self.window_size:
                    yield (row[0], self.preprocess_window(window))
                else:
                    yield (row[0], None)
            elif self.windowing_strategy == 'partial':
                yield (row[0], self.preprocess_window(window))

    def preprocess_window(self, window):
        preprocessed = self._preprocess_window(numpy.asanyarray(window))
        if self.output_window_length <= 0:
            return preprocessed[-1]
        else:
            return numpy.asanyarray(preprocessed[-self.output_window_length:])

    def _preprocess_window(self, window):
        raise NotImplementedError()

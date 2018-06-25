from collections import deque
import numpy


class Preprocessor():
    def __init__(self, preprocess_window_length=0, output_window_length=-1, **kwargs):
        self.output_single_rows = output_window_length <= 0
        self.output_window_length = output_window_length if output_window_length > 0 else 1

        self.prefetch_tick_count = max(preprocess_window_length, self.output_window_length)
        self.window = deque(maxlen=self.prefetch_tick_count)

    def init(self, initial_rows):
        self.window.extend(initial_rows)

    def append_row(self, row):
        self.window.append(row)

    def get_output(self):
        preprocessed = self._preprocess_window(numpy.asanyarray(self.window))
        if self.output_single_rows:
            return preprocessed[-1]
        else:
            return numpy.asanyarray(preprocessed[-self.output_window_length:])

    def append_and_preprocess(self, row):
        self.append_row(row)
        return self.get_output()

    def inverse_preprocess(row):
        raise NotImplementedError()

    def _preprocess_window(self, window):
        raise NotImplementedError()

    def get_state(self):
        return {}

    def reset_state(self):
        self.window.clear()

    def load_state(self):
        pass

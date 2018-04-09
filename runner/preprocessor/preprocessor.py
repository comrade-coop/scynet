from collections import deque
import numpy


class Preprocessor():
    def __init__(self, preprocess_window_length, output_window_length, **kwargs):
        self.prefetch_tick_count = max(preprocess_window_length, output_window_length)
        self.window = deque(maxlen=self.prefetch_tick_count)
        self.output_window_length = output_window_length

    def init(self, initial_rows):
        self.window.extend(initial_rows)

    def append_row(self, row):
        self.window.append(row)

    def get_output_window(self):
        return numpy.asanyarray(
            self._preprocess_window(
                numpy.asanyarray(self.window)
            )[-self.output_window_length:]
        )

    def append_and_preprocess(self, row):
        self.append_row(row)
        return self.get_output_window()

    def inverse_preprocess(row):
        raise NotImplementedError()

    def _preprocess_window(self, window):
        raise NotImplementedError()

    def get_state(self):
        return {}

    def reset_state(self):
        pass

    def load_state(self):
        pass

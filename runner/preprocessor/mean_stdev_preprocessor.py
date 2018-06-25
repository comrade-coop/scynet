from .preprocessor import Preprocessor
import numpy


class MeanStdevPreprocessor(Preprocessor):
    def __init__(self, normalization_constant=0.7, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.normalization_constant_atanh = numpy.arctanh(normalization_constant)

    def _preprocess_window(self, window):
        mean, stdev = window.mean(axis=0), window.std(axis=0)
        centered = window - mean
        stdev = numpy.maximum(stdev, 0.1)
        normalized = numpy.tanh(centered / stdev * self.normalization_constant_atanh)

        return normalized

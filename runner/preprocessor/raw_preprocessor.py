from .preprocessor import Preprocessor


class RawPreprocessor(Preprocessor):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

    def _preprocess_window(self, window):
        return window

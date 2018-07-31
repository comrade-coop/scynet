class Model:
    def __init__(self, descriptor):
        self.needed_signal_descriptors = {}

    def plot(self, file_basename):
        pass

    def train(self, trainer):
        raise NotImplementedError()

    def test(self, training_session):
        raise NotImplementedError()

    def save_weights(self, weights):
        raise NotImplementedError()

    def load_weights(self, weights):
        raise NotImplementedError()

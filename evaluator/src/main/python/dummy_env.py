import tensorflow.keras.backend as K
import random
from evaluator import Evaluator

class DummyEvaluator(Evaluator):
    def loss(y_true, y_pred):
        return random.uniform(0, 1)

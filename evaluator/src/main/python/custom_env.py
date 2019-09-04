from evaluator import Evaluator
import tensorflow.keras.backend as K

class CustomEvaluator(Evaluator):
    def loss(y_true, y_pred):
        return K.mean(K.square(y_pred - y_true), axis=-1)

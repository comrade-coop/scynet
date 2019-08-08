import abc


class Evaluator(abc.ABC):
    '''
        Evaluator interface that needs to be implemented by any custom evaluator
        ABC - Abstract Base Class
    '''
    @abc.abstractmethod
    def loss(self, y_true, y_pred):
        '''
            Should return a tensorflow differentiable function
        '''
        pass
import abc

class ReinforcementEvaluator(abc.ABC):
    '''
        Based on the OpenAI GYM standard
        Every decision behind this is exlpained https://arxiv.org/pdf/1606.01540.pdf

        TODO: We could even use directly the python gym library, it is compatible with Theano, Tensorflow, Torch etc.

        Interface
         - observables = {}
         - metadata = {}
    '''

    @abc.abstractmethod
    def reset(self):
        '''
            returns initial observations
        '''
        pass

    @abc.abstractmethod
    def step(self, action):
        '''
            executes each action and
            returns observables, reward, condition, ...
        '''
        pass

    # @abc.abstractmethod
    # def close():
    #     pass

class Evaluator(abc.ABC):
    '''
        Evaluator interface that needs to be implemented by any custom evaluator
        ABC - Abstract Base Class
        TODO Discuss
    '''
    @abc.abstractmethod
    def loss(self, y_true, y_pred):
        '''
            Should return a tensorflow differentiable function
        '''
        pass
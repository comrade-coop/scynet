import abc

class ReinforcementEnvironment(abc.ABC):
    '''
        Based on the OpenAI GYM standard

        Interface
         - observables = {}
         - get_observations: Observations {}
         - Other methods for modifying state and working with the environment
        Should implement different ways to engage and modify state and check observation data
    '''

    @abc.abstractmethod
    def reset():
        pass

    @abc.abstractmethod
    def get_observations():
        pass

    @abc.abstractmethod
    def get_initial_observations():
        pass
    
    @abc.abstractmethod
    def reward():
        '''
            Should return a reward based on the environment state
        '''
        pass
    pass

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

class Environment:
    '''
        TODO Think the OOP through
    '''
    pass
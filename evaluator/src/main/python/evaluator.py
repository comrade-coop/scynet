import abc

class ReinforcementEnvironment(abc.ABC):
    '''
        Based on the OpenAI GYM standard
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
    '''
        Interface
         - observables = {}
         - get_observations: Observations {}
         - Other methods for modifying state and working with the environment
        Should implement different ways to engage and modify state and check observation data
    '''
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


class REvaluator(abc.ABC):
    '''
        Evaluator interface that needs to be implemented by any reinforcement evaluator
        ABC - Abstract Base Class
        TODO Discuss
    '''
    @abc.abstractmethod
    def environment():
        '''
            Should return an already implemented ReinforcementEnvironment class and set self.environment
        '''
        pass
    

    @abc.abstractmethod
    def reward(self):
        '''
            Should return a reward based on the environment state
        '''
        pass
    
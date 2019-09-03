import abc

class ReinforcementEvaluator(abc.ABC):
    '''
        Based on the OpenAI GYM standard

        Interface
         - observables = {}
         - get_observations: Observations {}
         - Other methods for modifying state and working with the environment
        Should implement different ways to engage and modify state and check observation data
    '''

    @abc.abstractmethod
    def start_session():
        pass

    @abc.abstractmethod
    def end_session():
        pass

    @abc.abstractmethod
    def reset():
        pass

    @abc.abstractmethod
    def get_observations():
        pass

    @abc.abstractmethod
    def get_observation(x, y_pred):
        '''
            x is Identifier
            y_pred is the agent action
            
            returns y as the environment observation
        '''
        pass

    @abc.abstractmethod
    def get_agent_performance():
        '''
            returns performance: float
        '''
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
        TODO Think the OOP through
    '''

    @abc.abstractmethod
    def loss(self, y_true, y_pred):
        '''
            Should return a tensorflow differentiable function
        '''
        pass
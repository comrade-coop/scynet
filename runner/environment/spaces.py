import numpy as np


class SetSpace():
    def __init__(self, elements):
        self.elements = frozenset(elements)
        self.elements_list = list(self.elements)

    def __len__(self):
        return len(self.elements)

    def sample(self, seed=None):
        random = np.random.RandomState(seed)
        return self.elements_list[random.choice(len(self.elements_list))]

    def contains(self, x):
        return x in self.elements


class BoxSpace():
    # Adapted from https://github.com/openai/gym/blob/master/gym/spaces/box.py
    def __init__(self, low, high, shape=None):
        """
        Two kinds of valid input:
            Box(-1.0, 1.0, (3,4)) # low and high are scalars, and shape is provided
            Box(np.array([-1.0,-2.0]), np.array([2.0,4.0])) # low and high are arrays of the same shape
        """
        if shape is None:
            assert low.shape == high.shape
            self.low = low
            self.high = high
            self.shape = (1,)
        else:
            assert np.isscalar(low) and np.isscalar(high)
            self.low = low + np.zeros(shape)
            self.high = high + np.zeros(shape)
            self.shape = shape

    def sample(self, seed=None):
        random = np.random.RandomState(seed)
        return random.uniform(low=self.low, high=self.high, size=self.low.shape)

    def contains(self, x):
        x = np.asanyarray(x)
        return x.shape == self.shape and (x >= self.low).all() and (x <= self.high).all()


class TupleSpace():
    # Adapted from https://github.com/openai/gym/blob/master/gym/spaces/tuple.py
    def __init__(self, spaces):
        self.spaces = tuple(spaces)

    def sample(self):
        return tuple([space.sample() for space in self.spaces])

    def contains(self, x):
        if isinstance(x, list):
            x = tuple(x)

        return isinstance(x, tuple) and len(x) == len(self.spaces) and all(space.contains(part) for (space, part) in zip(self.spaces, x))

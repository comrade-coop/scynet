from .signal import Signal
from .adapters import adapters


class SignalReader(Signal):
    def __init__(self, source, name, component=None):
        self.adapter = adapters[source][name]
        self.component = component
        if component is None:
            super().__init__(self.adapter.shape, self.adapter.granularity, self.adapter.available_from, self.adapter.available_to)
        else:
            assert(self.component in self.adapter.components)
            super().__init__(self.adapter.shape[1:], self.adapter.granularity, self.adapter.available_from, self.adapter.available_to)

    def iterate(self, from_time, to_time):
        return self.adapter.iterate_component(self.component, from_time, to_time)

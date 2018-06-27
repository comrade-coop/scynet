

class _Wrapper():
    def __init__(self, iterator, preprocessor):
        self.current = (0, None)
        self.next = (0, None)
        self.iterator = iter(iterator)
        self.preprocessor = preprocessor

    def increment_if_on(self, date):
        if self.next[0] == date:
            if self.next[1] is not None:
                self.current = (
                    self.next[0],
                    self.preprocessor.append_and_preprocess(self.next[1])
                )
            self.next = self.iterator.__next__()


class SignalCombiner():
    def __init__(self, inputs):
        self.inputs = inputs
        self.available_from = max(signal.available_from for signal, component, preprocessor in self.inputs)
        self.available_to = min(signal.available_to for signal, component, preprocessor in self.inputs)
        self.granularity = min(signal.granularity for signal, component, preprocessor in self.inputs)

    def iterate(self, from_date, to_date, reset_preprocessors=True):
        if reset_preprocessors:
            for signal, component, preprocessor in self.inputs:
                preprocessor.reset_state()
                preprocess_to = from_date + signal.granularity * preprocessor.prefetch_tick_count
                preprocessor.init((thing[1] for thing in signal.iterate_component(component, from_date, preprocess_to)))
        pairs = [
            _Wrapper(signal.iterate_component(component, from_date, to_date), preprocessor)
            for signal, component, preprocessor in self.inputs
        ]
        while True:
            min_date = min(pair.next[0] for pair in pairs)
            for pair in pairs:
                pair.increment_if_on(min_date)
            current_date = max(pair.current[0] for pair in pairs)
            if current_date != 0:
                yield (current_date, [pair.current[1] for pair in pairs])

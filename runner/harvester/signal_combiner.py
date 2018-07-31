from .signal import Signal


class SignalCombiner(Signal):
    class Wrapper():
        def __init__(self, iterator):
            self.current = (0, None)
            self.next = (0, None)
            self.iterator = iter(iterator)

        def increment_if_on(self, date):
            if self.next[0] == date:
                if self.next[1] is not None:
                    self.current = self.next
                self.next = self.iterator.__next__()

    def __init__(self, signals):
        self.signals = signals
        super().__init__(
            [signal.shape for signal in self.signals],
            min(signal.granularity for signal in self.signals),
            max(signal.available_from for signal in self.signals),
            min(signal.available_to for signal in self.signals))

    def iterate(self, from_date, to_date):
        wrapped = [
            SignalCombiner.Wrapper(signal.iterate(from_date, to_date))
            for signal in self.signals
        ]
        while True:
            min_date = min(pair.next[0] for pair in wrapped)
            for pair in wrapped:
                pair.increment_if_on(min_date)
            current_date = max(pair.current[0] for pair in wrapped)
            if current_date != 0:
                yield (current_date, [pair.current[1] for pair in wrapped])

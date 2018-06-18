import pandas as pd


class SignalReader:
    def __init__(self, name, shape, components, granularity, available_from, available_to):
        self.name = name
        self.shape = shape
        self.components = components
        self.granularity = granularity
        self.available_from = available_from
        self.available_to = available_to
        self.signal_cache = {}

    def iterate(self, from_time, to_time):
        for tick in self._iterate_and_cache(from_time, to_time):
            yield tick

    def iterate_component(self, component, from_time, to_time):
        for tick in self._iterate_and_cache(from_time, to_time):
            component_tick = (tick[0], tick[1][self.components.index(component)])
            yield component_tick

    def _iterate_and_cache(self, from_time, to_time):
        filtered_cache = {k: v for (k, v) in self.signal_cache.items() if (from_time <= k <= to_time)}

        if len(filtered_cache) > 0:
            sorted_filtered_cache = sorted(filtered_cache)

            if from_time < sorted_filtered_cache[0]:
                # missing range (from_time - oldest_time_in_list)
                for tick in self._iterate(from_time, sorted_filtered_cache[0] - self.granularity):
                    self.signal_cache[tick[0]] = tick[1]
                    yield tick

            current_time = sorted_filtered_cache[0]
            cache_counter = 0

            while cache_counter < len(sorted_filtered_cache):
                current_cache_time = sorted_filtered_cache[cache_counter]
                if current_time == current_cache_time:
                    tick = (current_cache_time, self.signal_cache[current_cache_time])
                    current_time = current_time + self.granularity
                    cache_counter = cache_counter + 1
                    yield tick
                else:
                    # missing range (current_time -> current_cache_time - 1)
                    for tick in self._iterate(current_time, current_cache_time - self.granularity):
                        self.signal_cache[tick[0]] = tick[1]
                        yield tick

                    current_time = current_cache_time  # in next iteration we should yield current_cache_time

            # missing range (newest_time_in_list - to_time)
            if current_time < to_time:
                for tick in self._iterate(current_time, to_time):
                    self.signal_cache[tick[0]] = tick[1]
                    yield tick

        else:
            for tick in self._iterate(from_time, to_time):
                self.signal_cache[tick[0]] = tick[1]
                yield tick

    def _iterate(self, from_time, to_time):
        """ Returns a tuple (time, list[component])"""
        return NotImplementedError()

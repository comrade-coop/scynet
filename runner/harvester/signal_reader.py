import hashlib
import os
import json
import portalocker


class SignalReader:
    def __init__(self, name, shape, granularity, available_from, available_to, components=[], config={}, **kwargs):
        self.name = name
        self.shape = shape
        self.granularity = granularity
        self.available_from = available_from
        self.available_to = available_to
        self.components = components
        self.config = config
        self.signal_cache = None
        cache_key = '1|%s|%s|%s|%s|%s' % (self.name, ','.join(str(x) for x in self.shape), self.granularity, self.available_from, self.available_to)
        cache_key = hashlib.sha256(cache_key.encode('utf-8')).hexdigest()
        self.signal_cache_file = os.path.join(os.path.dirname(__file__), '../signals/%s.json' % cache_key)

    def create_cache(self):
        if os.path.isfile(self.signal_cache_file):
            return

        with open(self.signal_cache_file, 'a') as cache_file:
            portalocker.lock(cache_file, portalocker.LOCK_EX)
            self.signal_cache = {}
            for tick in self._iterate(self.available_from, self.available_to):
                self.signal_cache[str(tick[0])] = tick[1]

            json.dump(self.signal_cache, cache_file, separators=(',', ':'))

    def iterate(self, from_time, to_time):
        if not from_time < to_time:
            raise ValueError("from_time must be < to_time")

        if not (self.available_from <= from_time):
            raise ValueError("from_time should be after %s" % str(self.available_from))

        if not (to_time <= self.available_to):
            raise ValueError("to_time should be before %s" % str(self.available_to))

        if self.signal_cache is None:
            try:
                with open(self.signal_cache_file, 'r') as cache_file:
                    portalocker.lock(cache_file, portalocker.LOCK_SH)
                    self.signal_cache = json.load(cache_file)
            except Exception as e:
                self.create_cache()

        for current_time in range(from_time, to_time + 1, self.granularity):
            yield (current_time, self.signal_cache[str(current_time)])

    def iterate_component(self, component, from_time, to_time):
        if component is None:
            return self.iterate(from_time, to_time)
        else:
            return self._iterate_component(component, from_time, to_time)

    def _iterate_component(self, component, from_time, to_time):
        component = self.components.index(component)
        for tick in self.iterate(from_time, to_time):
            yield (tick[0], tick[1][component])

    def _iterate(self, from_time, to_time):
        """ Returns a tuple (time, list[component])"""
        return NotImplementedError()

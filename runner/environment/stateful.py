import json
import sys
from types import SimpleNamespace as Namespace

from .spaces import TupleSpace, BoxSpace, SetSpace


class Connector():
    def __init__(self, instream=sys.stdin, outstream=sys.stdout):
        self.i = instream
        self.o = outstream
        self.debug = 0  # random.randint(10, 99)

    def read(self):
        def _object_hook(d):
            return Namespace(**d)

        while True:
            line = self.i.readline()
            if line == '':
                return None
            try:
                return json.loads(
                    line,
                    object_hook=_object_hook
                )
            except Exception as e:
                print('Failed parsing line', line, file=sys.stderr)
                import time
                time.sleep(0.1)

    def write(self, *args, **kwargs):
        if len(args) != 0:
            self._write_dict(args[0])
        else:
            self._write_dict(kwargs)

    def _write_dict(self, dictionary):
        self.o.write(json.dumps(dict(dictionary), separators=(',', ':')))
        self.o.write('\n')
        self.o.flush()


class StatefulEnv():
    def __init__(self, preprocessor, config, input_stream=sys.stdin, output_stream=sys.stdout, *args, **kwargs):
        self.action_space = SetSpace([0, 1])
        self.connector = Connector(input_stream, output_stream)

        spaces = [BoxSpace(-1, 1, tuple(layer['config']['shape'])) for layer in config['layers'] if layer['type'] == 'Input']

        self.observation_space = TupleSpace(spaces)

        self.preprocessor = preprocessor

        self.last_action = 0
        self.debug_i = 0

    def step(self, action):
        if self.debug_i % 50 == 0:
            print('Env step %s' % self.debug_i, file=sys.stderr)
            self.debug_i += 1

        self.connector.write(action='buy' if action == 1 else 'sell')
        result = self.connector.read()

        if hasattr(result, 'values'):

            return (
                self.preprocessor.normalize(result.values),
                getattr(result, 'feedback', 0.0),
                False,
                {})
        else:
            return (self.reset(), 0.0, True, {})

    def reset(self):
        self.preprocessor.reset()

        self.connector.write(reset=True)

        self.connector.write(prefetch=self.preprocessor.prefetch_tick_count)
        prefetched_rows = []
        for i in range(self.preprocessor.prefetch_tick_count):
            prefetched_rows.append(self.connector.read().values)

        self.preprocessor.fit(prefetched_rows)

        result = self.connector.read()

        return self.preprocessor.normalize(result.values)

    def render(self, mode='human', close=False):
        self.connector.debug = 1

    def close(self):
        self.connector.write(close=True)

    def seed(self, seed=None):
        pass

    def configure(self, *args, **kwargs):
        pass

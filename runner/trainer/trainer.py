from ..harvester import SignalCombiner, parse_input_specification
from .training_session import TrainingSession
import logging


default_price_source = {'type': 'SignalReader', 'config': {'source': 'cryptocompare', 'name': 'market', 'component': 'close'}}
logger = logging.getLogger('trainer')


class Trainer:
    def __init__(self, signal_descriptors, price_signal_descriptor=default_price_source, default_trades_output=None):
        signals = [parse_input_specification(descriptor) for descriptor in signal_descriptors]
        self.signal = SignalCombiner(signals)
        self.price_signal = parse_input_specification(price_signal_descriptor)
        self.default_trades_output = default_trades_output

        self._create_episodes([
            ('learning', None),
            ('validation', 60 * 60 * 24 * 365 // 2),
            ('test', 60 * 60 * 24 * 365 // 2),
        ])

    def _create_episodes(self, episodes):
        self.episode_ranges = {}
        unspecified_timespan_count = 0
        unspecified_timespan = self.signal.available_to - self.signal.available_from
        for episode, timespan in episodes:
            if timespan is None:
                unspecified_timespan_count += 1
            else:
                unspecified_timespan -= timespan

        date_reached = self.signal.available_from
        for episode, timespan in episodes:
            if timespan is None:
                timespan = unspecified_timespan // unspecified_timespan_count

            self.episode_ranges[episode] = (date_reached, date_reached + timespan)
            date_reached += timespan

        logger.info('Loaded Data: ' + ', '.join('{days} {name} days'.format(
            name=episode,
            days=(date_range[1] - date_range[0]) // (60 * 60 * 24)
        ) for episode, date_range in self.episode_ranges.items()))

    def get_episode_row_count(self, episode):
        return (self.episode_ranges[episode][1] - self.episode_ranges[episode][0]) // self.signal.granularity

    def get_episode_iterators(self, episode):
        return (
            iter(self.price_signal.iterate(self.episode_ranges[episode][0], self.episode_ranges[episode][1])),
            iter(self.signal.iterate(self.episode_ranges[episode][0], self.episode_ranges[episode][1]))
        )

    def start_session(self, episode, trades_output=None):
        if trades_output is None:
            trades_output = self.default_trades_output
        return TrainingSession(self, episode, trades_output)

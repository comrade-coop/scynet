import os
import sys
import json
import random
import time
import logging
import importlib
import signal
# from zlib import adler32
from hashlib import md5

import tensorflow
from keras import backend as K
# from numpy.random import seed as numpy_seed
from .trainer import Trainer
from .harvester import parse_repositories


logger = logging.getLogger('main')


def main():
    currently_doing = ''
    try:
        currently_doing = 'getting input'
        json_conf, short_hash = get_inputs()
        agent_folder, files = get_files_location(short_hash)

        currently_doing = 'initializing'
        init(agent_folder, files)

        logger.info('Started agent')
        logger.info('Chromosome: %s\n%s\n', short_hash, json.dumps(json_conf, indent=4))

        currently_doing = 'building model'
        model, trainer = build_model(json_conf, files['model_preview'])

        def finish_up(signum, stack):
            logger.info('Received interrupt signal while %s, saving state', currently_doing)
            model.save_state(files['state'])
            sys.exit(0)

        signal.signal(signal.SIGINT, finish_up)
        signal.signal(signal.SIGTERM, finish_up)

        try:
            model.load_state(files['state'])
            logger.info('Loaded saves state')
        except OSError as exception:
            logger.info('Found no saved state')
            logger.debug('Got this exception while trying to read it:', exc_info=exception)

        while not model.is_trained():
            currently_doing = 'training'
            train(model, trainer)

        currently_doing = 'testing'
        validation_score = test(model, trainer, 'validation', files['trades'])
        test_score = test(model, trainer, 'test', files['trades'])

        currently_doing = 'saving results'

        model.save_state(files['state'])

        print('score = {result}'.format(result=validation_score))
        print('display_score = {result}'.format(result=test_score))
        # print('info = {info}'.format(info=model.get_training_info()))

        K.get_session().close()
    except BaseException as exception:
        if not isinstance(exception, SystemExit):
            logger.error('Got exception while %s!', currently_doing, exc_info=exception)
        return


def get_inputs():
    config_line = sys.stdin.readline()
    config = json.loads(config_line)

    short_hash = md5(config_line.encode('utf-8')).hexdigest()[0:10]

    return config, short_hash


def get_files_location(short_hash):
    agent_folder = 'results/running-%s' % short_hash
    for filename in os.listdir('results'):
        if short_hash in filename:
            agent_folder = 'results/%s' % filename
            break
    return (agent_folder, {
        'trades': agent_folder + '/trades.csv',
        'state': agent_folder + '/state',
        'log': agent_folder + '/log.log',
        'model_preview': agent_folder + '/model',
    })


def init(agent_folder, files):
    if not os.path.exists(agent_folder):
        os.makedirs(agent_folder)

    formatter = logging.Formatter('%(asctime)s [%(levelname)s] [%(name)s] %(message)s', '%Y-%m-%dT%H:%M:%S%z')
    formatter.converter = time.gmtime

    stderr_handler = logging.StreamHandler(sys.stderr)
    stderr_handler.setFormatter(formatter)

    file_handler = logging.FileHandler(files['log'], mode='a')
    file_handler.setFormatter(formatter)

    logging.getLogger().addHandler(stderr_handler)
    logging.getLogger().addHandler(file_handler)
    logging.getLogger().setLevel(logging.INFO)

    parse_repositories('repositories.json')

    tensorflow_config = tensorflow.ConfigProto()
    tensorflow_config.gpu_options.allow_growth = True
    tensorflow_config.gpu_options.visible_device_list = '%d' % random.randint(0, 3)
    tensorflow_session = tensorflow.Session(config=tensorflow_config)
    K.set_session(tensorflow_session)

    # numpy_seed(config.get('seed', adler32(config_line.encode('utf-8'), 1337)))


def build_model(json_conf, model_image_file):
    logger.info('Building model...')
    model_name = json_conf['type']
    model_package = importlib.import_module('.%s' % model_name, package=__package__)
    model = model_package.Model(json_conf)

    logger.info('Creating trainer...')
    trainer = Trainer(model.needed_signal_descriptors)

    logger.info('Plotting model...')
    model.plot(model_image_file)

    return model, trainer


def train(model, trainer):
    logger.info('Model is not trained yet, training...')
    model.train(trainer)
    logger.info('Training finished!')


def test(model, trainer, episode, trades_output_file):
    logger.info('Starting %s...', episode)

    trades_output = open(trades_output_file, 'a')

    session = trainer.start_session(episode, trades_output)
    model.test(session)
    score = session.get_result()

    logger.info('%s finished! Score: %f', episode.capitalize(), score)

    return score


if __name__ == '__main__':
    main()

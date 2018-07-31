import os
import sys
import json
import random
import time
import logging
# from zlib import adler32
from hashlib import md5

import tensorflow
from keras import backend as K
# from numpy.random import seed as numpy_seed
from .trainer import Trainer
from .rl import RLModel
from .harvester import parse_repositories


def main():
    currently_doing = ''
    try:
        currently_doing = 'getting input'
        json_conf, short_hash = get_inputs()
        agent_folder, files = get_files_location(short_hash)

        currently_doing = 'initializing'
        init(agent_folder, files)

        logging.info('Started agent')
        logging.info('Chromosome: %s\n%s\n', short_hash, json.dumps(json_conf, indent=4))

        currently_doing = 'building model'
        model, trainer = build_model(json_conf, files['model_preview'])

        if os.path.isfile(files['weights']):
            currently_doing = 'loading saved weights'
            model.load_weights(files['weights'])
        else:
            currently_doing = 'training'
            train(model, trainer)

        currently_doing = 'testing'
        validation_score = test(model, trainer, 'validation', files['trades'])
        test_score = test(model, trainer, 'test', files['trades'])

        currently_doing = 'saving results'

        model.save_weights(files['weights'])

        print('score = {result}'.format(result=validation_score))
        print('display_score = {result}'.format(result=test_score))
        # print('info = {info}'.format(info=model.get_training_info()))

        K.get_session().close()
    except BaseException as exception:
        logging.error('Got exception while %s!', currently_doing, exc_info=exception)
        return


def get_inputs():
    config_line = sys.stdin.readline()
    config = json.loads(config_line)

    short_hash = md5(config_line.encode('utf-8')).hexdigest()[0:10]

    return config, short_hash


def get_files_location(short_hash):
    agent_folder = 'results/running-%s' % short_hash
    return (agent_folder, {
        'trades': agent_folder + '/trades.csv',
        'weights': agent_folder + '/weights.h5f',
        'log': agent_folder + '/log.log',
        'model_preview': agent_folder + '/model',
    })


def init(agent_folder, files):
    if not os.path.exists(agent_folder):
        os.makedirs(agent_folder)

    logging.basicConfig(
        format='%(asctime)s [%(levelname)s] %(message)s',
        datefmt='%Y-%m-%dT%H:%M:%S%z',
        level=logging.INFO
    )
    logging._defaultFormatter.converter = time.gmtime

    parse_repositories('repositories.json')

    tensorflow_config = tensorflow.ConfigProto()
    tensorflow_config.gpu_options.allow_growth = True
    tensorflow_config.gpu_options.visible_device_list = '%d' % random.randint(0, 3)
    tensorflow_session = tensorflow.Session(config=tensorflow_config)
    K.set_session(tensorflow_session)

    # numpy_seed(config.get('seed', adler32(config_line.encode('utf-8'), 1337)))


def build_model(json_conf, model_image_file):
    logging.info('Building model...')
    model = RLModel(json_conf)

    logging.info('Creating trainer...')
    trainer = Trainer(model.needed_signal_descriptors)

    logging.info('Plotting model...')
    model.plot(model_image_file)

    logging.info('Done!')

    return model, trainer


def train(model, trainer):
    logging.info('Starting training...')
    model.train(trainer)
    logging.info('Training finished!')


def test(model, trainer, episode, trades_output_file):
    logging.info('Starting %s...', episode)

    trades_output = open(trades_output_file, 'a')

    session = trainer.start_session(episode, trades_output)
    model.test(session)
    score = session.get_result()

    logging.info('%s finished! Score: %f', episode.capitalize(), score)

    return score


if __name__ == '__main__':
    main()

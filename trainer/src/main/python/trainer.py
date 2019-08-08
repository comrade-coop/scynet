import sys
import numpy as np
import os
import json
import tensorflow as tf
from time import sleep
from model_parser.keras_parser import build_model, load_json
# import custom loss from the evaluator


class Trainer():
    '''
        'data' should be a dictionary that contains x, y data
            - data.x
            - data.y

        'config' should be a dictionary with the following structure
            - environment: Environment
            - type: Problem Type (reinforcement | classificaiton | unsupervised | semi-supervised)
    '''

    def __init__(self, data, config):
        self.data = data
        self.config = config
        self.environment = config['environment']
        pass

    def train(self, json_model, epochs=10, test_split=0.1, validation_split=0.1):
        
        # TODO Discuss python io - ignite process and add stdin input()
        keras_json = load_json(json_model)
        keras_json['loss'] = self.environment.loss
        keras_model, input_metadata = build_model(keras_json)

        data_size = len(self.data["x"])
        test_data_len = int(test_split * data_size)

        x_train = self.data["x"][test_data_len:]
        y_train = self.data["y"][test_data_len:]

        x_test = self.data["x"][:test_data_len]
        y_test = self.data["y"][:test_data_len]

        keras_model.summary()
        keras_model.fit(x_train, y_train, epochs=10, validation_split=validation_split)
        val_loss = keras_model.evaluate(x_test, y_test)

        # This is if we are going to use stdin TODO Discuss
        
        # print('score = ' + str(-val_loss), file=real_stdout) 
        # print('display_score = ' + str(-val_loss), file=real_stdout)


# NICE: Parsing works


# print(keras_model.predict(x_test))

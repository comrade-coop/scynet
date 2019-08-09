import sys
import numpy as np
import os
import json
import tensorflow as tf
from time import sleep
from model_parser.keras_parser import build_model, load_json
# import custom loss from the evaluator

# Tricks for the STD communication
# real_stdout = sys.stdout
# sys.stdout = sys.stderr
# 

class Trainer:
    '''
        'data' should be a dictionary that contains x, y data
            - data.x
            - data.y

        'config' should be a dictionary with the following structure
            - environment: Environment
            - is_executor: Boolean
            - type: Problem Type (reinforcement | classificaiton | unsupervised | semi-supervised)

        'split_strategy' should be a function that does the train,test,valthe 'data' dictionary
            - accepts validation and test coefficients (eg. 0.1 -> 10%)
            - returns x_train, y_train, x_test, y_test, validation_split_coefficient
        Check the source for more info
    '''

    def __init__(self, data, config, split_strategy=None):
        self.data = data
        self.config = config
        self.environment = config['environment']

        if split_strategy is not None:
            self.split_strategy = split_strategy

        # if self.config["is_executor"]:
            
    def predict(self, data=None):
        if data is None:
            return self.keras_model.predict(self.data["x_test"])
        else:
            return self.keras_model.predict(data)

    def swap_data(self, data):
        self.data = data
    
    def split_strategy(self, test_split, validation_split):
        
        data_size = len(self.data["x"])
        test_data_len = int(test_split * data_size)

        x_train = self.data["x"][test_data_len:]
        y_train = self.data["y"][test_data_len:]

        x_test = self.data["x"][:test_data_len]
        y_test = self.data["y"][:test_data_len]

        return x_train, y_train, x_test, y_test, validation_split
    
    def save_model(self):
        # TODO Implement
        pass

    def restore_model(self, json, weights):
        # TODO Implement
        pass

    def train(self, json_model, epochs, std=True):
        
        # TODO Discuss python io - ignite process and add stdin input()
        if not std: # if it is not True json_model should be input()
            keras_json = load_json(json_model)
        else:
            keras_json = json.loads(json_model)

        keras_json['loss'] = self.environment.loss
        self.keras_model, input_metadata = build_model(keras_json)
        
        x_train, y_train, x_test, y_test, validation_split = self.split_strategy(0.1, 0.1)

        self.data["x_train"] = x_train
        self.data["y_train"] = y_train 
        self.data["x_test"] = x_test
        self.data["y_test"] = y_test

        self.keras_model.summary()
        self.keras_model.fit(
            self.data["x_train"],
            self.data["y_train"],
            epochs=epochs,
            validation_split=validation_split
        )
        val_loss = self.keras_model.evaluate(self.data["x_test"], self.data["y_test"])

        # std communication TODO Discuss
        
        # print('score = ' + str(-val_loss), file=real_stdout) 
        # print('display_score = ' + str(-val_loss), file=real_stdout)
import pickle
import argparse

import numpy as np

def run(path, train_size=7*24, test_size=3*24):
    with open(path, 'rb') as f:
        ds = pickle.load(f)

        x_train = ds[0]['dataset']
        x_test = ds[1]['dataset']
        
        y_train, y_test = (ds[0]['labels'], ds[1]['labels'])

        autokeras_y = y_train[: int(y_train.shape[0]*8.0) ]
        ones = 0

        for i in range(autokeras_y.shape[0]):
            if autokeras_y[i] == 1:
                ones += 1

        ones = ones / autokeras_y.shape[0]

        print("Autokeras train set has %4f onces" % ones)

        assert(x_train.shape[0] == y_train.shape[0])
        assert(x_test.shape[0] == y_test.shape[0])

        start = x_train.shape[0]-train_size
        end = test_size        

        #x_train = x_train[start:]
        #y_train = y_train[start:]
        #print("Train intervals are %s; %s" % ( str(ds[0]['dates'][start]), str(ds[0]['dates'][-1])))

        #x_test = x_test[:test_size]
        #y_test = y_test[:test_size]
        #print("Test intervals are %s; %s" % ( str(ds[1]['dates'][0]), str(ds[1]['dates'][test_size-1]) ))

        return (x_train, y_train), (x_test, y_test)

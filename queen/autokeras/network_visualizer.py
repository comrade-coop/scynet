import argparse
import pickle

import keras
from keras.models import load_model

def run(path):
    graph = pickle.load(open(path, 'rb'))

    model = graph.produce_keras_model()

    model.compile(optimizer='adam', loss='categorical_crossentropy')

    #print(model.layers)

    print(model.input_shape)
    print(model.output_shape)

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="Prints model architecture and evaluates its performance.")
    parser.add_argument('path', help="Path to a .h5 model.")
    
    args, _ = parser.parse_known_args()

    run(args.path)

import argparse
import json
import traceback

import keras
from keras.layers import *  # we need all layers in the global namespace
from keras.optimizers import *
from keras.regularizers import *
from keras.models import Model
from keras.utils import plot_model
from .structure_exception import StructureException
from ..preprocessor import *


def load_json(filename):
    with open(filename) as f:
        return json.load(f)


def search_namespace(name):
    # TODO: Don't rely on Keras's class naming.
    # Have a dictionary from our names to keras's
    # TODO: What to do in a collision?
    return_class = globals().get(name)

    if return_class is None:
        raise StructureException("Invalid class name %s." % name)
    return return_class


def namespace_object(class_name, config):
    layer_class = search_namespace(class_name)
    return layer_class(**config)


def namespace_object_from_dict(dct):
    return namespace_object(dct['type'], dct['config'])


def keras_object(class_name, config):
    return namespace_object(class_name, config)


def process_input_layer(layer, idx, window_length):
    config = layer['config']

    preprocessor_obj = namespace_object_from_dict(config['preprocessor'])
    source_cfg = config['source']

    config = {
        'shape': (window_length,) + tuple(config['shape']),
        'name': source_cfg['name'].replace(',', '_') + '-' + str(idx)
    }

    layer_obj = keras_object(layer['type'], config)

    return layer_obj, (preprocessor_obj, source_cfg)


def process_non_input_layer(layer, idx, outputs):
    config = layer['config']
    layer_description = "(type: %s, ID: %d, inputs: %s)" % (layer['type'], idx, str(layer['inputs']))

    for key in config.keys():
        if isinstance(config[key], dict) and 'type' in config[key] and 'config' in config[key]:
            config[key] = namespace_object_from_dict(config[key])
        elif isinstance(config[key], dict) and 'type' in config[key] and config[key]['type'] == 'None':
            config[key] = None

    if len(layer['inputs']) == 0:
        raise StructureException("A non-input layer %s with no inputs." % layer_description)
    else:
        inputs = []
        used_input_indexes = []

        for inp in layer['inputs']:
            try:
                inputs.append(outputs[inp])
            except IndexError:
                raise StructureException("A layer %s, requesting inputs defined after it." % layer_description)

            used_input_indexes.append(inp)

        if len(inputs) == 1:  # don't pass a list of size 1
            inputs = inputs[0]

        layer_obj = keras_object(layer['type'], config)
        output = layer_obj(inputs)

    return output, used_input_indexes


def process_layers(structure):
    layers = structure['layers']
    window_length = structure['window_length']

    outputs = []  # tensor outputs from layers
    model_inputs = []  # global model input tensors
    input_metadata = []  # [(Preprocessor, signal config), (...), ...]
    used_as_input = {}  # ID -> True/False; whether the given layer has been used as an input

    for i, layer in enumerate(layers):
        used_as_input[i] = False  # default value
        # TODO: Handle stacked LSTM case (insert return_sequences when applicable)

        is_input = (layer['type'] == "Input")
        if is_input:
            output, metadata = process_input_layer(layer, i, window_length)

            input_metadata.append(metadata)
            model_inputs.append(output)
        else:
            output, layer_used_input_indexes = process_non_input_layer(layer, i, outputs)
            for k in layer_used_input_indexes:
                used_as_input[k] = True

        outputs.append(output)

    # all output tensors that haven't been used as an input in the graph are model outputs
    model_outputs = [outputs[i] for i in used_as_input if not used_as_input[i]]
    return model_inputs, model_outputs, input_metadata


def build_model(structure):
    model_inputs, model_outputs, input_metadata = process_layers(structure)

    model = Model(inputs=model_inputs, outputs=model_outputs)
    optimizer = structure['optimizer']
    optimizer_obj = keras_object(optimizer['type'], optimizer['config'])  # TODO: Don't rely on keras's optimizer namings
    model.compile(optimizer_obj, loss=structure['loss'])  # TODO: Don't rely on keras's loss namings

    return model, input_metadata


def run(filename):
    data = load_json(filename)
    try:
        model, input_metadata = build_model(data)  # This will raise an exception for invalid config
        plot_model(model, to_file="model.png")

        # print the metadata for test purposes
        for tup in input_metadata:
            print(tup[0], tup[1])

        return model
    except Exception:
        # TODO: List all possible exceptions, don't try to catch all exceptions (even system ones like MemoryError)
        print("Invalid config!")
        traceback.print_exc()


def init():
    parser = argparse.ArgumentParser(
        description="Module that loads a JSON neural network model and builds a keras model based on that.")
    parser.add_argument('input', help='The JSON file input.')
    args, _ = parser.parse_known_args()
    run(args.input)


if __name__ == "__main__":
    init()

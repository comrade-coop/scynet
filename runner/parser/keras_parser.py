import argparse
import json
import traceback

import keras
from keras.layers import * #we need all layers in the global namespace
from keras.optimizers import *
from keras.models import Model
from keras.utils import plot_model

from .structure_exception import StructureException

def loadJson(filename):
	with open(filename) as f:
		return json.load(f)

def getLayer(name):
	#TODO: Don't rely on Keras's class naming.
	#Have a dictionary from our names to keras's
	#TODO: What to do in a collision?
	returnClass = globals().get(name)

	if returnClass is None:
		raise StructureException("Invalid class name %s." % name)
	return returnClass

def kerasObject(className, config):
	layerClass = getLayer(className)
	#TODO: Don't rely on Keras's argument naming
	#Also have a translator dictionary

	return layerClass(**config)

def buildModel(structure):
	layers = structure['layers']
	outputs = [] #tensor outputs from layers

	modelInputs = [] #global model input tensors

	usedAsInput = {} # ID -> True/False; whether the given layer has been used as an input

	for i, layer in enumerate(layers):
		layerDescription = "(type: %s, ID: %d, inputs: %s)" % (layer['type'], i, str(layer['inputs']))
		usedAsInput[i] = False #default value

		isInput = False
		if layer['type'] == "Input":
			isInput = True

		config = layer['config']
		config.pop('normalization', None) #TODO: Handle this key
		config.pop('regularization', None) #TODO: This too
		#TODO: Handle stacked LSTM case (insert return_sequences when applicable)

		if isInput:
			config = {'shape':  config['shape']}

		layerObj = kerasObject(layer['type'], config)

		if isInput:
			output = layerObj
			modelInputs.append(layerObj)
		else:
			try:
				if len(layer['inputs']) == 0:
					raise StructureException("A non-input layer %s with no inputs." % layerDescription)
				else:
					inputs = []

					for inp in layer['inputs']:
						inputs.append(outputs[inp])
						usedAsInput[inp] = True

					if len(inputs) == 1: #don't pass a list of size 1
						inputs = inputs[0]
					output = layerObj(inputs)
			except IndexError:
				raise StructureException("A layer %s, requesting inputs defined after it." % layerDescription)
		outputs.append(output)

	#all output tensors that haven't been used as an input in the graph are model outputs
	modelOutputs = [outputs[i] for i in usedAsInput if not usedAsInput[i]]

	model = Model(inputs=modelInputs, outputs=modelOutputs)
	optimizer = structure['optimizer']
	optimizerObj = kerasObject(optimizer['type'], optimizer['config']) #TODO: Don't rely on keras's optimizer namigs
	model.compile(optimizerObj, loss=structure['loss']) #TODO: Don't rely on keras's loss namings

	return model

def run(filename):
	data = loadJson(filename)
	try:
		model = buildModel(data) #This will raise an exception for invalid config
		plot_model(model, to_file="model.png")
		return model
	except Exception: #TODO: List all possible exceptions, don't try to catch all exceptions (even system ones like MemoryError)
		print("Invalid config!")
		traceback.print_exc()


def init():
	parser = argparse.ArgumentParser(description="Module that loads a JSON neural network model and builds a keras model based on that.")
	parser.add_argument('input', help='The JSON file input.')

	args, _ = parser.parse_known_args()

	run(args.input)

if __name__ == "__main__":
	init()

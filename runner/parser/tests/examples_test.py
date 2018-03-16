import pytest
import sys, os

sys.path.insert(0, os.path.realpath('./'))

import keras_parser as parser

def test_examples():
	"""Test that runs all examples in the examples folder. They should complete successfully,
	meaning no exception throws"""
	examplesDir = './examples'
	examples = os.listdir(examplesDir)

	for example in examples:
		parser.run(os.path.join(examplesDir, example)) #this should not raise
		assert(True)

	for each example:
		model.input_shape == yes
		model.output_shape == yes...
from multiprocessing import Process, Value, Array
import time 

from keras.models import load_model
import pickle 
import numpy as np
import os


class KerasExecutor(Process):
	def __init__(self, uuid, egg, **kwargs):
		super(KerasExecutor, self).__init__()
		self.uuid = uuid
		self.egg = egg
		self.kwargs = kwargs

	def run(self):
		model = load_model("./model.h5")
		model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])

		while True:
			print("Heartbeat: " +  self.egg.eggData.decode())
			time.sleep(1)
from multiprocessing import Process, Value, Array
import time 

class KerasExecutor(Process):
	def __init__(self, uuid, egg, **kwargs):
		super(KerasExecutor, self).__init__()
		self.uuid = uuid
		self.egg = egg
		self.kwargs = kwargs

	def run(self):
		while True:
			print("Heartbeat: " +  self.egg.eggData.decode())
			time.sleep(1)




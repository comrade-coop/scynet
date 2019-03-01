import grpc

from concurrent import futures
import time
import math
import logging
import uuid

from autokeras_queen_agent import Queen
from keras_executor_agent import KerasExecutor
from multiprocessing.managers import BaseManager

from registry import AgentRegistry


_ONE_DAY_IN_SECONDS = 60 * 60 * 24

from Scynet.Component_pb2 import AgentStatusResponse, ListOfAgents
from Scynet.Component_pb2_grpc import ComponentServicer, add_ComponentServicer_to_server
from Scynet.Shared_pb2 import Void, Agent
from Scynet.Hatchery_pb2_grpc import HatcheryStub
from Scynet.Hatchery_pb2 import ComponentRegisterRequest, AgentRegisterRequest


class ComponentManager(BaseManager):
    pass

class ComponentFacade(ComponentServicer):
	def __init__(self, registry, hatchery):
		self.registry = registry
		self.hatchery = hatchery



	def AgentStart(self, request, context):
		if request.egg.agentType == "keras_executor": 
			agent = KerasExecutor(request.egg.uuid, request.egg)
		elif request.egg.agentType == "pytorch_executor":
			pass

		self.registry.start_agent(agent)
		return Void()

	def AgentStop(self, request, context):
		self.registry.stop_agent(request.uuid)
		return Void()
	

	def AgentStatus(self, request, context):
		return AgentStatusResponse(
			running=self.registry.is_running(request.uuid)
		)

	def AgentList (self, request, context):
		return ListOfAgents(agents=[agent.egg for agent in self.registry.get_all_agents()])

class LoggingInterceptor(grpc.ServerInterceptor):
	def __init__(self, logger):
		self._logger = logger 

	def intercept_service(self, continuation, handler_call_details):
		print(f"{handler_call_details.method}")
		return continuation(handler_call_details)


#TODO: Rewrite with: https://github.com/google/pinject
#TODO: Use this: https://github.com/BVLC/caffe/blob/master/python/caffe/io.py#L36
class Main:
	def __init__(self, port = 0):
		self.port = port
		self.channel = grpc.insecure_channel('localhost:9998')
		self.hatchery = HatcheryStub(self.channel)

	def register(self, port):
		#TODO: Better way to find the bound ip's
		self.component_uuid = uuid.uuid4()
		request = ComponentRegisterRequest(uuid=str(self.component_uuid), address=f"127.0.0.1:{port}" )
		request.runnerType[:] =["autokeras_queen", "keras_executor"]

		print( self.hatchery.RegisterComponent(request) )
		print( "Component registered." )

	def serve(self):

		with ComponentManager() as manager:
			registry = AgentRegistry(manager)

			logging_interceptor = LoggingInterceptor( logging.getLogger(__name__) )
			server = grpc.server(
				futures.ThreadPoolExecutor(max_workers=10),
				interceptors=(logging_interceptor,))
			add_ComponentServicer_to_server(
				ComponentFacade(registry, self.hatchery), server)

			self.port = server.add_insecure_port(f"0.0.0.0:{self.port}")
			self.register(self.port)
			
			server.start()
			print(f"Listening on: 127.0.0.1:{self.port}")
			
			queen = Queen()
			queen.start()

			print("Queen started, now producing agents")

			# I am doing this so I don't have to use make or sth simmilar.
			# TODO: Remove
			#import os
			#os.system(f'tmux new-window "exec bash ./test/test_queen.sh {self.port}"')

			

			try:
				while True:
					time.sleep(_ONE_DAY_IN_SECONDS)
			except KeyboardInterrupt:
				server.stop(0)


if __name__ == '__main__':
	logging.basicConfig()
	main = Main()
	main.serve()
import grpc

from concurrent import futures
import time
import math
import logging

from agent import Agent
from registry import AgentRegistry

_ONE_DAY_IN_SECONDS = 60 * 60 * 24

from Scynet.Component_pb2 import AgentStatusResponse, ListOfAgents
from Scynet.Component_pb2_grpc import ComponentServicer, add_ComponentServicer_to_server
from Scynet.Shared_pb2 import Void, Agent as AgentResponse

class ComponentFacade(ComponentServicer):
	def __init__(self, registry):
		self.registry = registry

	def AgentStart(self, request, context):
		agent = Agent(request.egg.uuid, request.egg)
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


def serve():
	registry = AgentRegistry()
	
	logging_interceptor = LoggingInterceptor( logging.getLogger(__name__) )

	server = grpc.server(
		futures.ThreadPoolExecutor(max_workers=10),
		interceptors=(logging_interceptor,))
	add_ComponentServicer_to_server(
		ComponentFacade(registry), server)

	port = server.add_insecure_port("0.0.0.0:0")
	server.start()
	print("Listening on: 127.0.0.1:{}".format(port))

	try:
		while True:
			time.sleep(_ONE_DAY_IN_SECONDS)
	except KeyboardInterrupt:
		server.stop(0)


if __name__ == '__main__':
	logging.basicConfig()
	serve()
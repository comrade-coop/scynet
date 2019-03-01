class AgentRegistry:
	def __init__(self, manager):
		self.running = {}
		self.manager = manager

	def start_agent(self, agent):
		self.running[agent.uuid] = agent
		agent.start()

	def stop_agent(self, id):
		if id in self.running:
			agent = self.running[id]
			agent.terminate()
			agent.join()

	def is_running(self, id):
		if id in self.running:
			agent = self.running[id]
			return agent.is_alive()
		else:
			return False

	def get_all_agents(self):
		running = self.running
		# return list(filter(lambda item: item.is_alive(), map(lambda item: item[1], running.items())))
		return [ agent for id, agent in running.items() if agent.is_alive() ] # Remove items that are not alive from the list. Can be rewrithen as a list comprehasnsion.


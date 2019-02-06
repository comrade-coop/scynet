using System;

namespace Grains
{
    using System;
    using System.Collections.Generic;
    using System.Text;
    using System.Threading;
    using System.Threading.Tasks;
    using GrainInterfaces;
    using Orleans;

    public class AgentRegistryState
    {
        public Dictionary<string, MockAgent> Agents = new Dictionary<string, MockAgent>();
    }


    public class AgentRegistryGrain : Grain<AgentRegistryState>, IAgentRegistryGrain
    {
        public AgentRegistryGrain()
        {

        }

        public Task AgentStart(MockEgg egg)
        {
            MockAgent agent = new MockAgent(egg);
            if(!State.Agents.ContainsKey(agent.Id))
            {
                agent.Start();
                State.Agents.Add(agent.Id, agent);
                return base.WriteStateAsync();
            }

            if(!State.Agents[agent.Id].IsRunning)
            {
                State.Agents[agent.Id].Start();
                return base.WriteStateAsync();
            }
            return Task.CompletedTask;
        }

        public Task AgentStop(string agentId)
        {
            if (State.Agents.ContainsKey(agentId))
            {
                State.Agents[agentId].Stop();
                Console.WriteLine("Agent with id" + agentId + " stopped.");
                return base.WriteStateAsync();
            }
            return Task.CompletedTask;
        }

        public Task GetAgentStatus(string agentId)
        {
            return Task.FromResult(State.Agents[agentId].IsRunning);
        }

        public Task GetAllAgents()
        {
            return Task.FromResult(State.Agents.Values);
        }
    }
}

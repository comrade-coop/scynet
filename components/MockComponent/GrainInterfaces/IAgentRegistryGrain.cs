using Orleans;
using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;

namespace GrainInterfaces
{
    public interface IAgentRegistryGrain : IGrainWithIntegerKey
    {
        Task AgentStart(MockAgent egg);
        Task AgentStop(string agentId);
        Task<bool> IsAgentRunning(string agentId);
        Task<List<MockAgent>> GetAllAgents();
    }
}

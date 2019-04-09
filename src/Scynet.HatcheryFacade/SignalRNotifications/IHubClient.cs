using Scynet.GrainInterfaces.Agent;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace Scynet.HatcheryFacade.SignalRNotifications
{
    public interface IHubClient
    {
        Task BroadcastMessage(string type, string payload);
        Task BroadcastNewAgent(AgentInfo agentInfo);
    }
}

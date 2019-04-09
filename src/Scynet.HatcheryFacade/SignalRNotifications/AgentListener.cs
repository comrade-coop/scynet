using Microsoft.AspNetCore.SignalR;
using Scynet.GrainInterfaces.Agent;
using Scynet.GrainInterfaces.Registry;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace Scynet.HatcheryFacade.SignalRNotifications
{
    public class AgentListener : IRegistryListener<Guid, AgentInfo>
    {
        private readonly IHubContext<NotifyHub, IHubClient> _hubContext;

        public AgentListener(IHubContext<NotifyHub, IHubClient> hubContext)
        {
            this._hubContext = hubContext;
        }

        public void NewItem(string queryIdentifier, Guid key, AgentInfo item)
        {
            this._hubContext.Clients.All.BroadcastNewAgent(item);
        }
    }
}

using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;
using Grpc.Core;
using Scynet;
using Component = Scynet.Component;
using Orleans;
using GrainInterfaces;

namespace Silo
{
    public class ComponentFacade : Component.ComponentBase
    {
        private readonly IClusterClient client;

        public ComponentFacade(IClusterClient client)
        {
            this.client = client;
        }

        public override Task<Scynet.Void> AgentStart(AgentStartRequest request, ServerCallContext context)
        {
            MockEgg egg = new MockEgg
            {
                Id = request.Uuid,
                Data = request.Egg.Data.ToByteArray()
            };
            var res = client.GetGrain<IAgentRegistryGrain>("0").AgentStart(egg);
            return base.AgentStart(request, context);
        }

        public override Task<Scynet.Void> AgentStop(AgentRequest request, ServerCallContext context)
        {
            client.GetGrain<IAgentRegistryGrain>("0").AgentStop(request.Uuid);
            return base.AgentStop(request, context);
        }

        public override Task<ListOfAgents> AgentList(AgentQuery request, ServerCallContext context)
        {
            var list = client.GetGrain<IAgentRegistryGrain>("0").GetAllAgents();
            return base.AgentList(request, context);
        }

        public override Task<AgentStatusResponse> AgentStatus(AgentRequest request, ServerCallContext context)
        {
            var status = client.GetGrain<IAgentRegistryGrain>("0").GetAgentStatus(request.Uuid);
            return base.AgentStatus(request, context);
        }

    }
}

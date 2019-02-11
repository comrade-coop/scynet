using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;
using Grpc.Core;
using Scynet;
using Component = Scynet.Component;
using Orleans;
using GrainInterfaces;

namespace Facade
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
            client.GetGrain<IAgentRegistryGrain>("0").AgentStart(egg).Wait();
            return Task.FromResult(new Scynet.Void());
        }

        public override Task<Scynet.Void> AgentStop(AgentRequest request, ServerCallContext context)
        {
            client.GetGrain<IAgentRegistryGrain>("0").AgentStop(request.Uuid).Wait();
            return Task.FromResult(new Scynet.Void()); ;
        }

        public override async Task<ListOfAgents> AgentList(AgentQuery request, ServerCallContext context)
        {
            var agentsList = await client.GetGrain<IAgentRegistryGrain>("0").GetAllAgents();
            ListOfAgents agentsListResponse = new ListOfAgents();
            foreach (var agent in agentsList)
            {
                var agentResponse = new Agent()
                {
                    Uuid = agent.Id,
                    Running = agent.IsRunning
                };
                agentsListResponse.Agents.Add(agentResponse);
            }

            return agentsListResponse;
        }

        public override async Task<AgentStatusResponse> AgentStatus(AgentRequest request, ServerCallContext context)
        {
            var status = await client.GetGrain<IAgentRegistryGrain>("0").GetAgentStatus(request.Uuid);
            return new AgentStatusResponse()
            {
                Running = status
            };
        }

    }
}

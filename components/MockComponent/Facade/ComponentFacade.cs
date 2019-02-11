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

        public override async Task<Scynet.Void> AgentStart(AgentStartRequest request, ServerCallContext context)
        {
            MockAgent egg = new MockAgent
            {
                Id = request.Egg.Uuid,
                EggData = request.Egg.EggData.ToByteArray()
            };
            await client.GetGrain<IAgentRegistryGrain>(0).AgentStart(egg);
            return new Scynet.Void();
        }

        public override async Task<Scynet.Void> AgentStop(AgentRequest request, ServerCallContext context)
        {
            await client.GetGrain<IAgentRegistryGrain>(0).AgentStop(request.Uuid);
            return new Scynet.Void(); ;
        }

        public override async Task<ListOfAgents> AgentList(AgentQuery request, ServerCallContext context)
        {
            var agentsList = await client.GetGrain<IAgentRegistryGrain>(0).GetAllAgents();
            ListOfAgents agentsListResponse = new ListOfAgents();
            foreach (var agent in agentsList)
            {
                var agentResponse = new Agent()
                {
                    Uuid = agent.Id
                };
                agentsListResponse.Agents.Add(agentResponse);
            }

            return agentsListResponse;
        }

        public override async Task<AgentStatusResponse> AgentStatus(AgentRequest request, ServerCallContext context)
        {
            var status = await client.GetGrain<IAgentRegistryGrain>(0).IsAgentRunning(request.Uuid);
            return new AgentStatusResponse()
            {
                Running = status
            };
        }

    }
}

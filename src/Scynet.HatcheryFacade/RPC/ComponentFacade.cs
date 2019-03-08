using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Grpc.Core;
using Microsoft.Extensions.Logging;
using Orleans;
using Scynet.GrainInterfaces;
using Scynet.GrainInterfaces.Agent;
using Scynet.GrainInterfaces.Component;
using Scynet.GrainInterfaces.Registry;

namespace Scynet.ComponentFacade.RPC
{
    public class ComponentFacade : Component.ComponentBase
    {
        private class FacadeEngager : IEngager
        {
            public void Released(IAgent agent)
            {
                // TODO: Notify hatchery
            }
        };

        private readonly ILogger<ComponentFacade> Logger;
        private readonly IClusterClient ClusterClient;
        private Task<IEngager> Engager;

        public ComponentFacade(ILogger<ComponentFacade> logger, IClusterClient clusterClient)
        {
            Logger = logger;
            ClusterClient = clusterClient;

            Engager = ClusterClient.CreateObjectReference<IEngager>(new FacadeEngager());
        }

        public override async Task<Void> RegisterInput(RegisterInputRequest request, ServerCallContext context)
        {
            var id = Guid.Parse(request.Input.Uuid);

            var agent = ClusterClient.GetGrain<IExternalAgent>(id);

            await agent.Initialize(new AgentInfo
            {
                ComponentId = Guid.Parse(request.Input.ComponentId),
                OutputShapes = request.Input.Outputs.Select(o => o.Dimension.ToList()).ToList(),
                Frequency = request.Input.Frequency,
                RunnerType = "external",
                Price = request.Agent.Price + 1,
                Agent = agent
            });

            return new Void();
        }

        public override async Task<Void> AgentStart(AgentStartRequest request, ServerCallContext context)
        {
            var id = Guid.Parse(request.Egg.Uuid);

            var registry = ClusterClient.GetGrain<IRegistry<Guid, AgentInfo>>(0);
            var agentInfo = await registry.Get(id);

            await agentInfo.Agent.Engage(await Engager);

            return new Void();
        }

        public override async Task<Void> AgentStop(AgentRequest request, ServerCallContext context)
        {
            var id = Guid.Parse(request.Uuid);

            var registry = ClusterClient.GetGrain<IRegistry<Guid, AgentInfo>>(0);
            var agentInfo = await registry.Get(id);

            await agentInfo.Agent.Release(await Engager);

            return new Void();
        }

        public override async Task<AgentStatusResponse> AgentStatus(AgentRequest request, ServerCallContext context)
        {
            var id = Guid.Parse(request.Uuid);

            var registry = ClusterClient.GetGrain<IRegistry<Guid, AgentInfo>>(0);
            var agentInfo = await registry.Get(id);

            return new AgentStatusResponse
            {
                Running = await agentInfo.Agent.IsRunning(),
            };
        }

        public override async Task<ListOfAgents> AgentList(AgentQuery request, ServerCallContext context)
        {
            var registry = ClusterClient.GetGrain<IRegistry<Guid, AgentInfo>>(0);

            // Maybe put the IsRunning part inside the Query somehow?
            var agents = await registry.Query(list => from agent in list where agent.Value.RunnerType != "external" select agent);

            var runningAgentsMap = await Task.WhenAll(agents.Select(async x => await x.Value.Agent.IsRunning()));
            var runningAgents = agents.Where((_, i) => runningAgentsMap[i]);

            return new ListOfAgents
            {
                Agents = { runningAgents.Select(agent => new Scynet.Agent
                {
                    Uuid = agent.Key.ToString(),
                    ComponentType = "hatchery",
                    ComponentId = Guid.Empty(), // TODO: Use own id here
                    Outputs = { agent.Value.OutputShapes.Select(i => new Shape { Dimension = { i } }) },
                    Frequency = agent.Value.Frequency,
                    Price = agent.Value.Price + 1,
                }).ToList() },
            };
        }
    }
}

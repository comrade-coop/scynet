using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Google.Protobuf;
using Grpc.Core;
using Microsoft.Extensions.Logging;
using Orleans;
using Scynet.GrainInterfaces;
using System.Security.Cryptography;

namespace Scynet.HatcheryFacade.RPC
{
    public class HatcheryFacade : Hatchery.HatcheryBase
    {
        private readonly ILogger<HatcheryFacade> Logger;
        private readonly IClusterClient ClusterClient;

        public HatcheryFacade(ILogger<HatcheryFacade> logger, IClusterClient clusterClient)
        {
            Logger = logger;
            ClusterClient = clusterClient;
        }

        public override async Task<ComponentRegisterResponse> RegisterComponent(ComponentRegisterRequest request, ServerCallContext context)
        {
            var id = Guid.Parse(request.Uuid);

            var component = ClusterClient.GetGrain<IComponent>(id);
            await component.Initialize(request.Address, new HashSet<String>(request.RunnerType));

            var registry = ClusterClient.GetGrain<IRegistry<ComponentInfo>>(0);
            await registry.Register(new ComponentInfo()
            {
                Id = id
            });

            return new ComponentRegisterResponse();
        }

        public override async Task<AgentRegisterResponse> RegisterAgent(AgentRegisterRequest request, ServerCallContext context)
        {
            var id = Guid.Parse(request.Agent.Uuid);
            var componentId = Guid.Parse(request.Agent.ComponentId);

            var data = request.Agent.EggData.ToByteArray();

            var agent = ClusterClient.GetGrain<IComponentAgent>(id);
            var component = ClusterClient.GetGrain<IComponent>(componentId);
            await agent.Initialize(component, request.Agent.ComponentType, data);

            var registry = ClusterClient.GetGrain<IRegistry<AgentInfo>>(0);
            await registry.Register(new AgentInfo()
            {
                Id = id,
                RunnerType = request.Agent.ComponentType,
            });

            return new AgentRegisterResponse();
        }

        public override async Task<Void> UnregisterComponent(ComponentUnregisterRequest request, ServerCallContext context)
        {
            var id = Guid.Parse(request.Uuid);

            var component = ClusterClient.GetGrain<IComponent>(id);
            await component.Disconnect();

            return new Void();
        }

        public override async Task<Void> AgentStopped(AgentStoppedEvent request, ServerCallContext context)
        {
            var id = Guid.Parse(request.Agent.Uuid);

            var agent = ClusterClient.GetGrain<IComponentAgent>(id);
            await agent.ReleaseAll();

            return new Void();
        }
    }
}

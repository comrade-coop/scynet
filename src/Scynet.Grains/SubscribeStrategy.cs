using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Orleans;
using Scynet.GrainInterfaces;

namespace Scynet.Grains
{
    public class SubscribeStrategyState
    {
        public ISet<IComponent> Components = new HashSet<IComponent>();
    }

    public class SubscribeStrategy : Orleans.Grain<SubscribeStrategyState>, ISubscribeStrategy, IRegistryListener<Guid, AgentInfo>
    {
        private readonly ILogger Logger;

        public SubscribeStrategy(ILogger<SubscribeStrategy> logger)
        {
            Logger = logger;
        }

        public async Task RegisterComponent(IComponent component)
        {
            State.Components.Add(component);
            await base.WriteStateAsync();
            await UpdateListener();
        }

        public async Task UnregisterComponent(IComponent component)
        {
            State.Components.Remove(component);
            await base.WriteStateAsync();
            await UpdateListener();
        }

        public async void NewItem(String @ref, Guid id, AgentInfo agentInfo)
        {
            if (@ref == typeof(SubscribeStrategy).FullName)
            {
                if (await ShouldSubscribe(id, agentInfo))
                {
                    var agent = GrainFactory.GetGrain<IAgent>(id);
                    await Task.WhenAll(State.Components.Select(component => component.RegisterInput(agent)));
                }
            }
        }

        private async Task UpdateListener()
        {
            var registry = GrainFactory.GetGrain<IRegistry<Guid, AgentInfo>>(0);
            if (State.Components.Count > 0)
            {
                await registry.Subscribe((id, agent) => true, this, typeof(SubscribeStrategy).FullName);
            }
            else
            {
                await registry.Unsubscribe(this, typeof(SubscribeStrategy).FullName);
            }
        }

        private async Task<bool> ShouldSubscribe(Guid id, AgentInfo agentInfo)
        {
            return true;
        }
    }
}

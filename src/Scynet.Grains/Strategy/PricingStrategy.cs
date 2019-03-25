using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Orleans;
using Scynet.GrainInterfaces.Component;
using Scynet.GrainInterfaces.Registry;
using Scynet.GrainInterfaces.Strategy;

namespace Scynet.Grains.Strategy
{
    public class PricingStrategyState
    {
        public ISet<IComponent> Components = new HashSet<IComponent>();
        public IAgentStrategyLogic<uint> Logic = new BasicPricingStrategy();
    }

    public class PricingStrategy : Orleans.Grain<PricingStrategyState>, IPricingStrategy, IRegistryListener<Guid, AgentInfo>
    {
        private readonly ILogger Logger;
        private readonly AgentStrategyLogicContext StrategyContext;

        public PricingStrategy(ILogger<PricingStrategy> logger)
        {
            Logger = logger;
            StrategyContext = new AgentStrategyLogicContext(GrainFactory);
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

        public async Task SetStrategyLogic(String type, String source)
        {
            await State.Logic.SetSource(source);
            await base.WriteStateAsync();
        }

        public async void NewItem(String @ref, Guid id, AgentInfo agentInfo)
        {
            if (@ref == typeof(PricingStrategy).FullName)
            {
                var newPrice = await State.Logic.Apply(id, agentInfo, StrategyContext);
                if (newPrice != 0)
                {
                    var agent = GrainFactory.GetGrain<IAgent>(id);
                    // TODO: Set price **before** the agent gets distributed to other components
                    await agent.SetPrice(newPrice);
                }
            }
        }

        private async Task UpdateListener()
        {
            var registry = GrainFactory.GetGrain<IRegistry<Guid, AgentInfo>>(0);
            if (State.Components.Count > 0)
            {
                var components = new HashSet<Guid>(State.Components.Select(component => component.GetPrimaryKey()));
                await registry.Subscribe(
                    (id, agent) => components.Contains(agent.ComponentId),
                    this,
                    typeof(PricingStrategy).FullName,
                    SubscriptionOptions.OnlyNew);
            }
            else
            {
                await registry.Unsubscribe(this, typeof(PricingStrategy).FullName);
            }
        }
    }
}

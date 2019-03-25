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
    public class InputOutputStrategyState
    {
        public ISet<IComponent> Components = new HashSet<IComponent>();
        public IAgentStrategyLogic<bool> Logic = new BasicInputOutputStrategy();
    }

    public class InputOutputStrategy : Orleans.Grain<InputOutputStrategyState>, IInputOutputStrategy, IRegistryListener<Guid, AgentInfo>
    {
        private readonly ILogger Logger;
        private readonly AgentStrategyLogicContext StrategyContext;

        public InputOutputStrategy(ILogger<InputOutputStrategy> logger)
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
            if (@ref == typeof(InputOutputStrategy).FullName)
            {
                if (await State.Logic.Apply(id, agentInfo, StrategyContext))
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
                await registry.Subscribe((id, agent) => true, this, typeof(InputOutputStrategy).FullName);
            }
            else
            {
                await registry.Unsubscribe(this, typeof(InputOutputStrategy).FullName);
            }
        }
    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Orleans;
using Orleans.Runtime;
using Scynet.GrainInterfaces.Component;
using Scynet.GrainInterfaces.Registry;
using Scynet.GrainInterfaces.Strategy;

namespace Scynet.Grains.Strategy
{
    public class HibernateStrategyState
    {
        public ISet<IComponent> Components = new HashSet<IComponent>();
        public TimeSpan UpdateFrequency = new TimeSpan(0, 10, 0);
        public IAgentStrategyLogic<bool> Logic = new BasicHibernateStrategy();
    }

    public class HibernateStrategy : Orleans.Grain<HibernateStrategyState>, IHibernateStrategy, IRemindable
    {
        private readonly ILogger Logger;
        private readonly AgentStrategyLogicContext StrategyContext;

        public HibernateStrategy(ILogger<HibernateStrategy> logger)
        {
            Logger = logger;
            StrategyContext = new AgentStrategyLogicContext(GrainFactory);
        }

        public async Task RegisterComponent(IComponent component)
        {
            State.Components.Add(component);
            await base.WriteStateAsync();
            await UpdateReminder();
        }

        public async Task UnregisterComponent(IComponent component)
        {
            State.Components.Remove(component);
            await base.WriteStateAsync();
            await UpdateReminder();
        }

        public async Task SetUpdateFrequency(TimeSpan updateFrequency)
        {
            State.UpdateFrequency = updateFrequency;
            await base.WriteStateAsync();
            await UpdateReminder();
        }

        public async Task SetStrategyLogic(String type, String source)
        {
            await State.Logic.SetSource(source);
            await base.WriteStateAsync();
        }

        private async Task UpdateReminder()
        {
            if (State.Components.Count > 0)
            {
                await RegisterOrUpdateReminder(typeof(HibernateStrategy).FullName, State.UpdateFrequency, State.UpdateFrequency);
            }
            else
            {
                await UnregisterReminder(await GetReminder(typeof(HibernateStrategy).FullName));
            }
        }

        public async Task ReceiveReminder(String @ref, TickStatus status)
        {
            if (@ref == typeof(HibernateStrategy).FullName)
            {
                var components = new HashSet<Guid>(State.Components.Select(component => component.GetPrimaryKey()));

                var registry = GrainFactory.GetGrain<IRegistry<Guid, AgentInfo>>(0);
                var agents = await registry.Query(l =>
                    from i in l
                    where components.Contains(i.Value.ComponentId)
                    select i
                );

                await Task.WhenAll(agents.Select(pair => TryHibernate(pair.Key, pair.Value)));
            }
        }

        private async Task TryHibernate(Guid id, AgentInfo agentInfo)
        {
            if (await State.Logic.Apply(id, agentInfo, StrategyContext))
            {
                var agent = GrainFactory.GetGrain<IAgent>(id);
                await agent.ReleaseAll();
            };
            await base.WriteStateAsync();
        }
    }
}

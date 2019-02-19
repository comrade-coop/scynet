using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Orleans;
using Scynet.GrainInterfaces;

namespace Scynet.Grains
{
    public class PublishStrategyState
    {
        public ISet<IComponent> Components = new HashSet<IComponent>();
    }

    public class PublishStrategy : Orleans.Grain<PublishStrategyState>, IPublishStrategy, IRegistryListener<Guid, AgentInfo>
    {
        private readonly ILogger Logger;

        public PublishStrategy(ILogger<PublishStrategy> logger)
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
            if (@ref == typeof(PublishStrategy).FullName)
            {
                if (await ShouldPublish(id, agentInfo))
                {
                    var agent = GrainFactory.GetGrain<IAgent>(id);
                    // HACK: Don't use a fake publisher...
                    var publisher = GrainFactory.GetGrain<FakePublisher>(0);
                    publisher.PublishAgent(agent);
                }
            }
        }

        private async Task UpdateListener()
        {
            var registry = GrainFactory.GetGrain<IRegistry<Guid, AgentInfo>>(0);
            if (State.Components.Count > 0)
            {
                var components = new HashSet<Guid>(State.Components.Select(component => component.GetPrimaryKey()));
                await registry.Subscribe((id, agent) => components.Contains(agent.ComponentId), this, typeof(PublishStrategy).FullName);
            }
            else
            {
                await registry.Unsubscribe(this, typeof(PublishStrategy).FullName);
            }
        }

        private async Task<bool> ShouldPublish(Guid id, AgentInfo agentInfo)
        {
            return true;
        }
    }

    // HACK: Fake publisher, just in order to have an IEngager to give to IAgent::Engage
    public interface IFakePublisher : Orleans.IGrainWithIntegerKey
    {
        Task PublishAgent(IAgent agent);
    }

    public class FakePublisher : Orleans.Grain, IFakePublisher, IEngager
    {
        private readonly ILogger Logger;

        public FakePublisher(ILogger<FakePublisher> logger)
        {
            Logger = logger;
        }

        public async Task PublishAgent(IAgent agent)
        {
            Logger.LogInformation($"Agent fake-published ({agent})!");
            await agent.Engage(this);
        }

        public void Released(IAgent agent)
        {
            Logger.LogInformation($"Fake-published agent hibernated ({agent})!");
        }
    }
}

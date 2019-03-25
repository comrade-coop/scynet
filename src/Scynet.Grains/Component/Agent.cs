using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Orleans;
using Scynet.GrainInterfaces.Registry;
using Scynet.GrainInterfaces.Component;

namespace Scynet.Grains.Component
{
    public class AgentState
    {
        public AgentInfo Info = new AgentInfo();
        public Dictionary<IEngager, EngagementInfo> Engagements = new Dictionary<IEngager, EngagementInfo>();
        public bool Running = false;
        public List<IAgent> Inputs;
        public byte[] Data = { };
    }

    public class Agent : Grain<AgentState>, IAgent, IEngager
    {
        private readonly ILogger Logger;

        public Agent(ILogger<Agent> logger)
        {
            Logger = logger;
        }

        /// <inheritdoc/>
        public async Task Initialize(AgentInfo info, IEnumerable<IAgent> inputs, byte[] data)
        {
            State.Info = info;
            State.Data = data;
            State.Inputs = inputs.ToList();

            var registry = GrainFactory.GetGrain<IRegistry<Guid, AgentInfo>>(0);
            await registry.Register(this.GetPrimaryKey(), State.Info);

            await base.WriteStateAsync();
        }

        public async Task SetPrice(uint price)
        {
            State.Info.Price = price;

            // Every time the price is updated, we re-register the agent
            var registry = GrainFactory.GetGrain<IRegistry<Guid, AgentInfo>>(0);
            await registry.Register(this.GetPrimaryKey(), State.Info);

            await base.WriteStateAsync();
        }

        /// <inheritdoc/>
        public Task<IComponent> GetComponent()
        {
            var component = GrainFactory.GetGrain<IComponent>(State.Info.ComponentId);
            return Task.FromResult(component);
        }

        /// <inheritdoc/>
        public Task<byte[]> GetData()
        {
            return Task.FromResult(State.Data);
        }

        /// <inheritdoc/>
        public Task<IEnumerable<IAgent>> GetInputs()
        {
            return Task.FromResult((IEnumerable<IAgent>)State.Inputs);
        }

        /// <inheritdoc/>
        public async void Released(IAgent agent)
        {
            if (State.Running && State.Inputs.Contains(agent))
            {
                await ReleaseAll();
            }
        }

        /// <inheritdoc/>
        public Task<IEnumerable<EngagementInfo>> GetActiveEngagements()
        {
            return Task.FromResult(State.Engagements.Values.ToList() as IEnumerable<EngagementInfo>);
        }

        /// <inheritdoc/>
        public async Task Engage(IEngager engager)
        {
            Logger.LogInformation($"Agent engaged ({engager})!");
            State.Engagements[engager] = new EngagementInfo()
            {
                Engager = engager,
                EngagedSince = DateTime.UtcNow
            };
            await base.WriteStateAsync();
            if (!State.Running)
            {
                await Task.WhenAll(State.Inputs.Select(input => input.Engage(this)));

                var component = GrainFactory.GetGrain<IComponent>(State.Info.ComponentId);
                await component.StartAgent(this);

                State.Running = true;
            }
        }

        /// <inheritdoc/>
        public Task Release(IEngager engager)
        {
            Logger.LogInformation($"Agent released ({engager})!");
            engager.Released(this);
            State.Engagements.Remove(engager);
            return base.WriteStateAsync();
        }

        /// <inheritdoc/>
        public async Task ReleaseAll()
        {
            Logger.LogInformation($"Releasing all agents!");

            foreach (IEngager engager in State.Engagements.Keys)
            {
                engager.Released(this);
            }
            State.Engagements.Clear();

            await base.WriteStateAsync();

            if (State.Running)
            {
                await Task.WhenAll(State.Inputs.Select(input => input.Release(this)));

                var component = GrainFactory.GetGrain<IComponent>(State.Info.ComponentId);
                await component.StopAgent(this);

                State.Running = false;
            }
        }

        /// <inheritdoc/>
        public Task<String> GetTopic()
        {
            return Task.FromResult(this.GetPrimaryKey().ToString());
        }
    }
}

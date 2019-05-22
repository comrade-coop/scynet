using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Orleans;
using Orleans.Providers;
using Scynet.GrainInterfaces.Agent;
using Scynet.GrainInterfaces.Registry;

namespace Scynet.Grains.Agent
{
    public class AgentState
    {
        public AgentInfo Info = new AgentInfo();
        public Dictionary<IEngager, EngagementInfo> Engagements = new Dictionary<IEngager, EngagementInfo>();
        public bool Running = false;
    }

    public abstract class Agent<T> : Grain<T>, IAgent where T : AgentState, new()
    {

        private DateTime lastRegistryUpdate = default(DateTime);
        /// <summary>
        /// Start running agent
        /// </summary>
        abstract public Task Start();

        /// <summary>
        /// Stop running agent
        /// </summary>
        abstract public Task Stop();

        /// <summary>
        /// Check if the agent is running
        /// </summary>
        abstract public Task<bool> IsRunning();

        private readonly ILogger Logger;

        public Agent(ILogger<Agent<T>> logger)
        {
            Logger = logger;
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
            try {
                if (!State.Running || !(await IsRunning()))
                {
                    await Start();
                    State.Running = true;
                }
            } finally {
                await base.WriteStateAsync();
            }

            var evalutor = GrainFactory.GetGrain<IEvaluator>(this.GetPrimaryKey());
            await evalutor.Start(this);
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

            try {
                if (State.Running)
                {
                    await Stop();
                    State.Running = false;
                }
            } finally {
                await base.WriteStateAsync();
            }
        }

        /// <inheritdoc/>
        public Task<String> GetTopic()
        {
            return Task.FromResult(this.GetPrimaryKey().ToString());
        }

        protected async Task UpdateRegistryInfo(bool critical = true)
        {
            if (critical || lastRegistryUpdate < DateTime.Now - TimeSpan.FromSeconds(30)) {
                lastRegistryUpdate = DateTime.Now;
                var registry = GrainFactory.GetGrain<IRegistry<Guid, AgentInfo>>(0);
                await registry.Register(this.GetPrimaryKey(), State.Info);
            }
        }

        /// <inheritdoc/>
        public async Task SetMetadata(string key, string value, bool critical=false)
        {
            State.Info.Metadata[key] = value;

            await base.WriteStateAsync();
            await UpdateRegistryInfo(critical);
        }
    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Orleans;
using Scynet.GrainInterfaces;

namespace Scynet.Grains
{
    public class AgentState
    {
        public Dictionary<IEngager, EngagementInfo> Engagements = new Dictionary<IEngager, EngagementInfo>();
        public bool Running = false;
    }

    public abstract class Agent<T> : Grain<T>, IAgent where T : AgentState, new()
    {
        /// <summary>
        /// Start running agent
        /// </summary>
        abstract public Task Start();

        /// <summary>
        /// Stop running agent
        /// </summary>
        abstract public Task Stop();

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
            await base.WriteStateAsync();
            if (!State.Running)
            {
                await Start();
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
                await Stop();
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

using System;
﻿using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Orleans;
using Scynet.GrainInterfaces;

namespace Scynet.Grains
{
    public class ComponentAgentState {
        public Dictionary<IEngager, EngagementInfo> Engagements = new Dictionary<IEngager, EngagementInfo>();
    }

    public class ComponentAgent : Grain<ComponentAgentState>, IAgent
    {
        private readonly ILogger Logger;

        public ComponentAgent(ILogger<ComponentAgent> logger)
        {
            Logger = logger;
        }

        /// <inheritdoc/>
        public Task<IEnumerable<EngagementInfo>> GetActiveEngagements() {
            return Task.FromResult(State.Engagements.Values.ToList() as IEnumerable<EngagementInfo>);
        }

        /// <inheritdoc/>
        public Task Engage(IEngager engager) {
            Logger.LogInformation($"Agent engaged ({engager})!");
            State.Engagements[engager] = new EngagementInfo() {
                Engager = engager,
                EngagedSince = DateTime.UtcNow
            };
            return base.WriteStateAsync();
        }

        /// <inheritdoc/>
        public Task Release(IEngager engager) {
            Logger.LogInformation($"Agent released ({engager})!");
            engager.Released(this);
            State.Engagements.Remove(engager);
            return base.WriteStateAsync();
        }

        /// <inheritdoc/>
        public Task ReleaseAll() {
            Logger.LogInformation($"Releasing all agents!");
            foreach (IEngager engager in State.Engagements.Keys) {
                engager.Released(this);
            }
            State.Engagements.Clear();
            return base.WriteStateAsync();
        }

        /// <inheritdoc/>
        public Task<String> GetTopic() {
            return Task.FromResult(this.GetPrimaryKey().ToString());
        }
    }
}

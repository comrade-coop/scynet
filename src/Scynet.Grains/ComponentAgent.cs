using System;
﻿using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Scynet.GrainInterfaces;

namespace Scynet.Grains
{
    public class ComponentAgentState {
        public Dictionary<int, Engagement> Engagements;
    }

    public class ComponentAgent : Orleans.Grain<ComponentAgentState>, IAgent
    {
        private readonly ILogger Logger;

        public ComponentAgent(ILogger<ComponentAgent> logger)
        {
            Logger = logger;
            State.Engagements = new Dictionary<int, Engagement>();
        }

        public Task<IEnumerable<Engagement>> GetActiveEngagements() {
            return Task.FromResult(State.Engagements.Values.ToList() as IEnumerable<Engagement>);
        }

        public Task Engage(Engagement engagement) {
            Logger.LogInformation($"Agent engaged ({engagement})!");
            State.Engagements[engagement.Id] = engagement;
            return base.WriteStateAsync();
        }

        public Task Release(Engagement engagement) {
            Logger.LogInformation($"Agent released ({engagement})!");
            State.Engagements.Remove(engagement.Id);
            return base.WriteStateAsync();
        }
    }
}

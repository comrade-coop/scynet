using System;
﻿using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Scynet.GrainInterfaces;

namespace Scynet.Grains
{
    public class ComponentState {
        public String Endpoint;
        public String RunnerType;
        public IList<Guid> Inputs;
    }

    public class Component : Orleans.Grain<ComponentState>, IComponent
    {
        private readonly ILogger Logger;

        public Component(ILogger<Component> logger)
        {
            Logger = logger;
        }

        public Task Initialize(String endpoint, String runnerType) {
            State.Endpoint = endpoint;
            State.RunnerType = runnerType;
            State.Inputs = new List<Guid>();
            return base.WriteStateAsync();
        }

        public Task RegisterInput(Guid agentId) {
            State.Inputs.Add(agentId);
            // TODO: Send input to endpoint
            return base.WriteStateAsync();
        }

        public Task<Guid> CreateAgent(EggDescriptor egg) {
            var agentId = Guid.NewGuid();
            // TODO: Initialize agent
            return Task.FromResult(agentId);
        }
    }
}

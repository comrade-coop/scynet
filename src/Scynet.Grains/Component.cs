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
        public ISet<String> RunnerTypes;
        public IList<Guid> Inputs = new List<Guid>();
    }

    public class Component : Orleans.Grain<ComponentState>, IComponent
    {
        private readonly ILogger Logger;

        public Component(ILogger<Component> logger)
        {
            Logger = logger;
        }

        public Task Initialize(String endpoint, ISet<String> runnerTypes) {
            State.Endpoint = endpoint;
            State.RunnerTypes = new HashSet<String>(runnerTypes);
            return base.WriteStateAsync();
        }

        public Task Disconnect() {
            State.Endpoint = "";
            return base.WriteStateAsync();
        }

        public Task RegisterInput(Guid agentId) {
            State.Inputs.Add(agentId);
            // TODO: Send input to endpoint
            return base.WriteStateAsync();
        }
    }
}

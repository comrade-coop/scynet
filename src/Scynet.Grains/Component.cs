using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Scynet.GrainInterfaces;

namespace Scynet.Grains
{
    public class ComponentState
    {
        public String Address;
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

        public Task Initialize(String address, ISet<String> runnerTypes)
        {
            State.Address = address;
            State.RunnerTypes = new HashSet<String>(runnerTypes);
            return base.WriteStateAsync();
        }

        public Task<string> GetAddress()
        {
            return Task.FromResult(State.Address);
        }

        public Task Disconnect()
        {
            State.Address = "";
            return base.WriteStateAsync();
        }

        public Task RegisterInput(Guid agentId)
        {
            State.Inputs.Add(agentId);
            // TODO: Send input to address
            return base.WriteStateAsync();
        }
    }
}

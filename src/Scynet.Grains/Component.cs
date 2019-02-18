using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Grpc.Core;
using Microsoft.Extensions.Logging;
using Orleans;
using Scynet.GrainInterfaces;

namespace Scynet.Grains
{
    public class ComponentState
    {
        public String Address;
        public ComponentInfo Info = new ComponentInfo();
        public IList<IAgent> Inputs = new List<IAgent>();
    }

    public class Component : Orleans.Grain<ComponentState>, IComponent
    {
        private readonly ILogger Logger;
        private Lazy<Channel> Channel;

        public Component(ILogger<Component> logger)
        {
            Logger = logger;
            Channel = new Lazy<Channel>(() => new Channel(State.Address, ChannelCredentials.Insecure));
        }

        public async Task Initialize(String address, ISet<String> runnerTypes)
        {
            State.Address = address;
            State.Info.RunnerTypes = new HashSet<String>(runnerTypes);

            var registry = GrainFactory.GetGrain<IRegistry<Guid, ComponentInfo>>(0);
            await registry.Register(this.GetPrimaryKey(), State.Info);

            await base.WriteStateAsync();
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

        public async Task RegisterInput(IAgent agent)
        {
            State.Inputs.Add(agent);

            await base.WriteStateAsync();

            var registry = GrainFactory.GetGrain<IRegistry<Guid, AgentInfo>>(0);
            var info = await registry.Get(agent.GetPrimaryKey());

            var client = new Scynet.Component.ComponentClient(Channel.Value);

            await client.RegisterInputAsync(new RegisterInputRequest
            {
                Input = info.ToProtobuf(agent.GetPrimaryKey())
            });
        }
    }
}

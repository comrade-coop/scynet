using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Grpc.Core;
using Microsoft.Extensions.Logging;
using Orleans;
using Scynet.GrainInterfaces.Component;
using Scynet.GrainInterfaces.Registry;
using Scynet.Grains.Component;

namespace Scynet.Grains.Component
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
        private Channel _Channel;

        public Component(ILogger<Component> logger)
        {
            Logger = logger;
            _Channel = null;
        }

        public async Task Initialize(String address, ISet<String> runnerTypes)
        {
            State.Address = address;
            State.Info.RunnerTypes = new HashSet<String>(runnerTypes);

            _Channel = null;

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
            _Channel = null;
            return base.WriteStateAsync();
        }

        private Channel GetChannel()
        {
            if (_Channel == null)
            {
                _Channel = new Channel(State.Address, ChannelCredentials.Insecure);
            }
            return _Channel;
        }

        /// <inheritdoc/>
        public async Task RegisterInput(IAgent agent)
        {
            State.Inputs.Add(agent);

            await base.WriteStateAsync();

            var registry = GrainFactory.GetGrain<IRegistry<Guid, AgentInfo>>(0);
            var info = await registry.Get(agent.GetPrimaryKey());

            var client = new Scynet.Component.ComponentClient(GetChannel());

            await client.RegisterInputAsync(new RegisterInputRequest
            {
                Input = info.ToProtobuf(agent.GetPrimaryKey())
            });
        }

        /// <inheritdoc/>
        public async Task StartAgent(IAgent agent)
        {
            State.Inputs.Add(agent);

            await base.WriteStateAsync();

            var registry = GrainFactory.GetGrain<IRegistry<Guid, AgentInfo>>(0);
            var infoTask = registry.Get(agent.GetPrimaryKey());
            var dataTask = agent.GetData();
            var inputsTask = agent.GetInputs();

            await Task.WhenAll(infoTask, dataTask, inputsTask);

            var client = new Scynet.Component.ComponentClient(GetChannel());

            await client.AgentStartAsync(new AgentStartRequest
            {
                Egg = infoTask.Result.ToProtobuf(
                    agent.GetPrimaryKey(),
                    dataTask.Result,
                    inputsTask.Result.Select(i => i.GetPrimaryKey())
                )
            });
        }

        /// <inheritdoc/>
        public async Task StopAgent(IAgent agent)
        {
            var client = new Scynet.Component.ComponentClient(GetChannel());

            await client.AgentStopAsync(new AgentRequest
            {
                Uuid = agent.GetPrimaryKey().ToString()
            });

        }
    }
}

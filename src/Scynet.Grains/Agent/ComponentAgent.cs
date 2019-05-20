using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Grpc.Core;
using Microsoft.Extensions.Logging;
using Orleans;
using Scynet.GrainInterfaces.Agent;
using Scynet.GrainInterfaces.Component;
using Scynet.GrainInterfaces.Registry;

namespace Scynet.Grains.Agent
{
    public class ComponentAgentState : AgentState
    {
        public byte[] Data = { };
        public List<IAgent> Inputs;
    }

    public class ComponentAgent : Agent<ComponentAgentState>, IComponentAgent, IEngager
    {
        private readonly ILogger Logger;
        private Channel Channel = null;

        public ComponentAgent(ILogger<ComponentAgent> logger) : base(logger)
        {
            Logger = logger;
        }

        /// <inheritdoc/>
        public async Task Initialize(AgentInfo info, IEnumerable<IAgent> inputs, byte[] data)
        {
            State.Info = info;
            State.Data = data;
            State.Inputs = inputs.ToList();

            await base.WriteStateAsync();
            await UpdateRegistryInfo();
            var evalutor = GrainFactory.GetGrain<IEvaluator>(this.GetPrimaryKey());
            await evalutor.Start(this);
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



        private async Task<Channel> GetChannel()
        {
            if (Channel != null && (Channel.State == ChannelState.TransientFailure || Channel.State == ChannelState.Shutdown))
            {
                Channel = null;
            }

            if (Channel == null)
            {

                var component = GrainFactory.GetGrain<IComponent>(State.Info.ComponentId);
                var address = await component.GetAddress();

                if (string.IsNullOrEmpty(address))
                {
                    await Channel.ShutdownAsync();
                    // TODO: Remove when needed, and don't just choose the first one.
                    var registry = GrainFactory.GetGrain<IRegistry<Guid, ComponentInfo>>(0);
                    var viable = await registry.Query(components =>
                        from c in components
                        where c.Value.RunnerTypes.Contains(State.Info.RunnerType)
                        select c);

                    address = await GrainFactory.GetGrain<IComponent>(viable.First().Key).GetAddress();

                    var agentRegistry = GrainFactory.GetGrain<IRegistry<Guid, AgentInfo>>(0);
                    State.Info.ComponentId = viable.First().Key;
                    await base.WriteStateAsync();

                    await UpdateRegistryInfo();
                }

                Channel = new Channel(address, ChannelCredentials.Insecure);
            }


            return Channel;
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
        public override async Task Start()
        {
            var client = new Scynet.Component.ComponentClient(await GetChannel());

            await Task.WhenAll(State.Inputs.Select(input => input.Engage(this)));

            await client.AgentStartAsync(new AgentStartRequest
            {
                Egg = State.Info.ToProtobuf(
                    this.GetPrimaryKey(),
                    State.Data,
                    State.Inputs.Select(i => i.GetPrimaryKey())
                )
            });

            var evalutor = GrainFactory.GetGrain<IEvaluator>(this.GetPrimaryKey());
            await evalutor.Start(this);
        }

        /// <inheritdoc/>
        public override async Task Stop()
        {
            var client = new Scynet.Component.ComponentClient(await GetChannel());

            await client.AgentStopAsync(new AgentRequest
            {
                Uuid = this.GetPrimaryKey().ToString()
            });

            Channel = null;

            await Task.WhenAll(State.Inputs.Select(input => input.Release(this)));
        }
    }
}

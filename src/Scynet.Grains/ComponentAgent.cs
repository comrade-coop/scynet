using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Google.Protobuf;
using Grpc.Core;
using Microsoft.Extensions.Logging;
using Orleans;
using Scynet.GrainInterfaces;

namespace Scynet.Grains
{
    public class ComponentAgentState : AgentState
    {
        public IComponent Component;
        public string RunnerType;
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
        public Task Initialize(IComponent component, string runnerType, IEnumerable<IAgent> inputs, byte[] data)
        {
            State.Component = component;
            State.RunnerType = runnerType;
            State.Data = data;
            State.Inputs = inputs.ToList();
            return base.WriteStateAsync();
        }

        /// <inheritdoc/>
        public Task<IComponent> GetComponent()
        {
            return Task.FromResult(State.Component);
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
            if (Channel == null)
            {
                var address = await State.Component.GetAddress();
                Channel = new Channel(address, ChannelCredentials.Insecure);
            }
            return Channel;
        }

        /// <inheritdoc/>
        public async void Released(IAgent agent)
        {
            if (State.Running && State.Inputs.Contains(agent)) {
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
                Egg = new Agent
                {
                    Uuid = this.GetPrimaryKey().ToString(),
                    EggData = ByteString.CopyFrom(State.Data),
                    ComponentType = State.RunnerType,
                    ComponentId = State.Component.GetPrimaryKey().ToString(),
                    Inputs = { State.Inputs.Select(i => i.GetPrimaryKey().ToString()) }
                }
            });
        }

        /// <inheritdoc/>
        public override async Task Stop()
        {
            var client = new Scynet.Component.ComponentClient(await GetChannel());

            await client.AgentStopAsync(new AgentRequest
            {
                Uuid = this.GetPrimaryKey().ToString()
            });

            await Task.WhenAll(State.Inputs.Select(input => input.Release(this)));
        }
    }
}

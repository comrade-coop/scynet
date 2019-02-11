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
    }

    public class ComponentAgent : Agent<ComponentAgentState>, IComponentAgent
    {
        private readonly ILogger Logger;
        private Channel Channel = null;

        public ComponentAgent(ILogger<ComponentAgent> logger) : base(logger)
        {
            Logger = logger;
        }

        /// <inheritdoc/>
        public Task Initialize(IComponent component, string runnerType, byte[] data)
        {
            State.Component = component;
            State.RunnerType = runnerType;
            State.Data = data;
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
        public override async Task Start()
        {
            var client = new Scynet.Component.ComponentClient(await GetChannel());

            await client.AgentStartAsync(new AgentStartRequest
            {
                Egg = new Agent
                {
                    Uuid = this.GetPrimaryKey().ToString(),
                    EggData = ByteString.CopyFrom(State.Data),
                    ComponentType = State.RunnerType,
                    ComponentId = State.Component.GetPrimaryKey().ToString()
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
        }
    }
}

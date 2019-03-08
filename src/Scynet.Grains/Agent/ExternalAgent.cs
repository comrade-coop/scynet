using System;
using System.Threading.Tasks;
using Grpc.Core;
using Microsoft.Extensions.Logging;
using Orleans;
using Scynet.GrainInterfaces.Agent;
using Scynet.GrainInterfaces.Registry;

namespace Scynet.Grains.Agent
{
    public class ExternalAgentState : AgentState
    {
        public AgentInfo Info = new AgentInfo();
        // public string Address;
    }

    public class ExternalAgent : Agent<ExternalAgentState>, IExternalAgent
    {
        private readonly ILogger Logger;

        public ExternalAgent(ILogger<ExternalAgent> logger) : base(logger)
        {
            Logger = logger;
        }

        /// <inheritdoc/>
        public async Task Initialize(AgentInfo info)
        {
            State.Info = info;
            // State.Address = address;

            var registry = GrainFactory.GetGrain<IRegistry<Guid, AgentInfo>>(0);
            await registry.Register(this.GetPrimaryKey(), State.Info);

            await base.WriteStateAsync();
        }

        // /// <inheritdoc/>
        // public Task<string> GetAddress()
        // {
        //     return Task.FromResult(State.Address);
        // }

        /// <inheritdoc/>
        public override Task Start()
        {
            return Task.FromException(new NotImplementedException());
        }

        /// <inheritdoc/>
        public override Task Stop()
        {
            return Task.FromException(new NotImplementedException());
        }
    }
}

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
    public class ExternalAgentState : AgentState
    {
        public string Address;
    }

    public class ExternalAgent : Agent<ExternalAgentState>, IExternalAgent
    {
        private readonly ILogger Logger;

        public ExternalAgent(ILogger<ExternalAgent> logger) : base(logger)
        {
            Logger = logger;
        }

        /// <inheritdoc/>
        public Task Initialize(string address)
        {
            State.Address = address;
            return base.WriteStateAsync();
        }

        /// <inheritdoc/>
        public Task<string> GetAddress()
        {
            return Task.FromResult(State.Address);
        }

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

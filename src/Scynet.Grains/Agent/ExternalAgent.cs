using System;
using System.Linq;
using System.Threading.Tasks;
using Grpc.Core;
using Microsoft.Extensions.Logging;
using Orleans;
using Scynet.GrainInterfaces.Agent;
using Scynet.GrainInterfaces.Registry;
using Scynet.GrainInterfaces.Facade;

namespace Scynet.Grains.Agent
{
    public class ExternalAgentState : AgentState
    {
        public string Address;
        public IFacade Facade;
        public DateTime StartedAt;
    }

    public class ExternalAgent : Agent<ExternalAgentState>, IExternalAgent
    {
        private readonly ILogger Logger;

        public ExternalAgent(ILogger<ExternalAgent> logger) : base(logger)
        {
            Logger = logger;
        }

        /// <inheritdoc/>
        public async Task Initialize(AgentInfo info, string address)
        {
            if (State.Running)
            {
                await Stop();
            }

            State.Address = address;
            State.Info = info;

            await UpdateRegistryInfo();

            await base.WriteStateAsync();

            if (State.Running)
            {
                await Start();
            }
        }

        /// <inheritdoc/>
        public Task<string> GetAddress()
        {
            return Task.FromResult(State.Address);
        }

        /// <inheritdoc/>
        public override async Task<bool> IsRunning()
        {
            if (DateTime.Now - State.StartedAt > TimeSpan.FromMinutes(20)) {
                await Stop();
                await Start();
            }
            return true;
        }

        /// <inheritdoc/>
        public override async Task Start()
        {
            // TODO: This code is not reliable.
            var now = DateTime.Now;
            // var maxLatency = TimeSpan.FromMinutes(1.5);
            var registry = GrainFactory.GetGrain<IRegistry<Guid, FacadeInfo>>(0);
            var activeFacades = (await registry.Query(l =>
                from i in l
                    // where (now - i.Value.LastUpdate) < maxLatency
                select i.Value.Facade)).ToList();
            Logger.LogInformation($"Found {activeFacades.Count()} facades");
            State.Facade = activeFacades[(new Random()).Next(activeFacades.Count())];

            State.Facade.Start(this); // <- can't await this
            State.StartedAt = DateTime.Now;

            await base.WriteStateAsync();
        }

        /// <inheritdoc/>
        public override async Task Stop()
        {
            State.Facade?.Stop(this);
            State.Facade = null;

            await base.WriteStateAsync();
        }
    }
}

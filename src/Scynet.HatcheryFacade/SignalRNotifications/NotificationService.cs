using Microsoft.AspNetCore.SignalR;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Orleans;
using Scynet.GrainInterfaces.Agent;
using Scynet.GrainInterfaces.Registry;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace Scynet.HatcheryFacade.SignalRNotifications
{
    public class NotificationService : IHostedService
    {
        private readonly IClusterClient _clusterClient;
        private readonly ILogger<NotificationService> _logger;
        private IHubContext<NotifyHub, IHubClient> _hubContext;

        public NotificationService(IClusterClient clusterClient, ILogger<NotificationService> logger,
            IHubContext<NotifyHub, IHubClient> hubContext)
        {
            _clusterClient = clusterClient;
            _logger = logger;
            _hubContext = hubContext;

            SubscribeToregistry();
        }

        public async void SubscribeToregistry()
        {
            //var listener = new AgentListener(_hubContext);
            var listener = await this._clusterClient.CreateObjectReference<IRegistryListener<Guid, AgentInfo>>(
                new AgentListener(_hubContext)
            );
            var registry = this._clusterClient.GetGrain<IRegistry<Guid, AgentInfo>>(0);
            await registry.Subscribe((k, v) => true, listener, "NewAgentListener");
        }

        public Task StartAsync(CancellationToken cancellationToken)
        {
            return Task.CompletedTask;
        }

        public Task StopAsync(CancellationToken cancellationToken)
        {
            return Task.CompletedTask;
        }
    }
}

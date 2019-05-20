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
    public class NotificationService : IHostedService, IRegistryListener<Guid, AgentInfo>
    {
        private readonly IClusterClient ClusterClient;
        private IHubContext<NotifyHub, INotifyHubClient> HubContext;

        public NotificationService(IClusterClient clusterClient, ILogger<NotificationService> logger,
            IHubContext<NotifyHub, INotifyHubClient> hubContext)
        {
            ClusterClient = clusterClient;
            HubContext = hubContext;

            SubscribeToRegistry();
        }

        public async void SubscribeToRegistry()
        {
            var listener = await this.ClusterClient.CreateObjectReference<IRegistryListener<Guid, AgentInfo>>(
                this
            );
            var registry = this.ClusterClient.GetGrain<IRegistry<Guid, AgentInfo>>(0);
            await registry.Subscribe((k, v) => true, listener, "NewAgent");
        }

        public void NewItem(string queryIdentifier, Guid key, AgentInfo item)
        {
            if (queryIdentifier == "NewAgent") {
                this.HubContext.Clients.All.BroadcastNewAgent(key, item);
            }
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

using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Grpc.Core;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;

namespace Scynet.HatcheryFacade.RPC
{
    // TODO: Move this file so it can be shared more easily.
    class GrpcBackgroundService : IHostedService
    {
        private readonly IEnumerable<Server> _servers;
        private readonly ILogger<GrpcBackgroundService> _logger;

        public GrpcBackgroundService(
            IEnumerable<Server> servers,
            ILogger<GrpcBackgroundService> logger
        )
        {
            _servers = servers;
            _logger = logger;
        }

        public Task StartAsync(CancellationToken cancellationToken)
        {
            _logger.LogDebug("Starting gRPC background service");

            foreach (var server in _servers)
            {
                StartServer(server);
            }

            _logger.LogInformation("gRPC background service started");

            return Task.CompletedTask;
        }



        public async Task StopAsync(CancellationToken cancellationToken)
        {
            _logger.LogDebug("Stopping gRPC background service");

            var shutdownTasks = _servers
                .Select(server => server.ShutdownAsync()).ToList();

            await Task.WhenAll(shutdownTasks).ConfigureAwait(false);

            _logger.LogInformation("gRPC background service stopped");
        }

        private void StartServer(Server server)
        {
            _logger.LogDebug(
                "Starting gRPC server listening on: {hostingEndpoints}",
                string.Join(
                    "; ",
                    server.Ports.Select(p => $"{p.Host}:{p.BoundPort}")
                )
            );

            server.Start();

            _logger.LogInformation(
                "Started gRPC server listening on: {hostingEndpoints}",
                string.Join(
                    "; ",
                    server.Ports.Select(p => $"{p.Host}:{p.BoundPort}")
                )
            );
        }
    }

}

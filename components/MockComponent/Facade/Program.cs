using System;
using System.Threading.Tasks;
using Grpc.Core;
using Microsoft.Extensions.Logging;
using Orleans;
using Orleans.Configuration;
using Orleans.Runtime;

namespace Silo
{
    class Program
    {
        const int initializeAttemptsBeforeFailing = 5;
        private static int attempt = 0;

        static void Main(string[] args)
        {
            RunMainAsync().GetAwaiter().GetResult();
        }

        private static async Task RunMainAsync()
        {
            var client = await StartClientWithRetries();

            var server = new Server()
            {
                Services =
                {
                    Scynet.Component.BindService(new ComponentFacade(client))
                },
                Ports = { new ServerPort("0.0.0.0", 0, ServerCredentials.Insecure) }
            };

            Console.WriteLine("Starting facade...");
            server.Start();
        }

        private static async Task<IClusterClient> StartClientWithRetries()
        {
            attempt = 0;
            var clientBuilder = new ClientBuilder()
                .UseLocalhostClustering()
                .Configure<ClusterOptions>(options =>
                {
                    options.ClusterId = "dev";
                    options.ServiceId = "SiloService";
                })
                .ConfigureLogging(logging => logging.AddConsole());

            // Add streaming
            //clientBuilder.AddSimpleMessageStreamProvider("StreamProviderName");
            var client = clientBuilder.Build();

            await client.Connect(RetryFilter);
            Console.WriteLine("Client successfully connect to silo host");
            return client;
        }

        private static async Task<bool> RetryFilter(Exception exception)
        {
            if (exception.GetType() != typeof(SiloUnavailableException))
            {
                Console.WriteLine($"Cluster client failed to connect to cluster with unexpected error.  Exception: {exception}");
                return false;
            }
            attempt++;
            Console.WriteLine($"Cluster client attempt {attempt} of {initializeAttemptsBeforeFailing} failed to connect to cluster.  Exception: {exception}");
            if (attempt > initializeAttemptsBeforeFailing)
            {
                return false;
            }
            await Task.Delay(TimeSpan.FromSeconds(4));
            return true;
        }
    }
}

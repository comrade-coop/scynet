using System;
using System.Threading.Tasks;
using System.Threading;
using Grpc.Core;
using Microsoft.Extensions.Logging;
using Orleans;
using Orleans.Configuration;
using Orleans.Runtime;
using GrainInterfaces;
using System.Text;
using Scynet;
using Google.Protobuf;

namespace Facade
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
            var grain = client.GetGrain<IAgentRegistryGrain>(0);
            MockAgent egg = new MockAgent()
            {
                Id = "sheny",
                EggData = Encoding.ASCII.GetBytes("Agent1")
            };

            await grain.AgentStart(egg);
            var agentsList = await grain.GetAllAgents();


            const int port = 50051;
            const string host = "localhost";
            var server = new Server()
            {
                Services =
                {
                    Scynet.Component.BindService(new ComponentFacade(client))
                },
                Ports = { new ServerPort(host, port, ServerCredentials.Insecure) }
            };


            Console.WriteLine("Starting facade...");
            server.Start();
            Console.WriteLine("Facade started");        

            //hatchery channel
            Channel channel = new Channel("127.0.0.1:9998", ChannelCredentials.Insecure);
            var hatcheryClient = new Scynet.Hatchery.HatcheryClient(channel);

            var componentId = new Guid().ToString();
            ComponentRegisterRequest hatcheryComponentRegisterRequest = new ComponentRegisterRequest()
            {
                Uuid = componentId,
                Address = host + ":" + port,
            };

            //add await
            hatcheryClient.RegisterComponent(hatcheryComponentRegisterRequest);

            Agent agent = new Agent()
            {
                Uuid = new Guid().ToString(),
                ComponentId = componentId,
                EggData = ByteString.CopyFrom("Agent1", Encoding.Unicode)
            };
            AgentRegisterRequest arr = new AgentRegisterRequest();
            arr.Agent = agent;
            hatcheryClient.RegisterAgent(arr);

            await server.ShutdownAsync();
        }

        private static async Task<IClusterClient> StartClientWithRetries()
        {
            attempt = 0;
            var clientBuilder = new ClientBuilder()
                .UseLocalhostClustering()
                .Configure<ClusterOptions>(options =>
                {
                    options.ClusterId = "ShenyCluster";
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

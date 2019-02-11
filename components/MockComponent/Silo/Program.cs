using System;
using System.Threading.Tasks;
using Orleans;
using Orleans.Configuration;
using Orleans.Hosting;
using Microsoft.Extensions.Logging;
using System.Net;
using Grains;

namespace Silo
{
    class Program
    {
        public static int Main(string[] args)
        {
            return RunMainAsync().Result;
        }

        private static async Task<int> RunMainAsync()
        {
            try
            {
                var host = await StartSilo();
                Console.WriteLine("Press Enter to terminate...");
                Console.ReadLine();

                await host.StopAsync();

                return 0;
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return 1;
            }
        }

        private static async Task<ISiloHost> StartSilo()
        {
            var builder = new SiloHostBuilder()
            // Use localhost clustering for a single local silo
            .UseLocalhostClustering()
            // Configure ClusterId and ServiceId
            .Configure<ClusterOptions>(options =>
            {
                options.ClusterId = "dev";
                options.ServiceId = "SiloService";
            })
            // Configure connectivity
            .Configure<EndpointOptions>(options => options.AdvertisedIPAddress = IPAddress.Loopback)
            .ConfigureApplicationParts(parts => parts.AddApplicationPart(typeof(AgentRegistryGrain).Assembly).WithReferences())
            // Configure logging with any logging framework that supports Microsoft.Extensions.Logging.
            // In this particular case it logs using the Microsoft.Extensions.Logging.Console package.
            .ConfigureLogging(logging => logging.AddConsole())
            //need to configure a grain storage called "PubSubStore" for using streaming with ExplicitSubscribe pubsub type
            //.AddMemoryGrainStorage("PubSubStore");
            .AddMemoryGrainStorage("Default");
            // Depends on your application requirements, you can configure your silo with other stream providers, which can provide other 
            // features, such as persistence or recoverability. 
            // For more information, please see http://dotnet.github.io/orleans/Documentation/Orleans-Streams/Stream-Providers.html
            //.AddSimpleMessageStreamProvider("ChatStreamProvider");

            var host = builder.Build();
            await host.StartAsync();
            return host;
        }
    }
}

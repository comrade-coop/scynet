using System;
using System.Threading.Tasks;
using Grpc.Core;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Orleans;
using Orleans.Configuration;
using Orleans.Hosting;
using BlockchainHarvester.Grains;

namespace BlockchainHarvester
{
    class Program
    {
        public static int Main(string[] args)
        {
            return RunMainAsync(args).Result;
        }

        private static async Task<int> RunMainAsync(string[] args)
        {
            try
            {
                Console.WriteLine("Configuring local silo...");

                Channel channel = new Channel("127.0.0.1:9998", ChannelCredentials.Insecure); // TODO: Make the ip configurable.

                var server = new Server()
                {
                    Services =
                    {
                        Scynet.Component.BindService(new ComponentFacade())
                    },
                    Ports = { new ServerPort("0.0.0.0", 0, ServerCredentials.Insecure) }
                };

                var builder = new SiloHostBuilder()
                    .UseLocalhostClustering()
                    .Configure<ClusterOptions>(options =>
                    {
                        options.ClusterId = "dev";
                        options.ServiceId = "EthereumBlockChainHarvester";
                    })
                    .ConfigureApplicationParts(parts => parts
                        .AddApplicationPart(typeof(BlockChainExtractor).Assembly)
                        .WithReferences()) //TODO: Load the assembly in some other way so we are not dependent on the implementation project.
                    .ConfigureLogging(logging => logging.AddConsole())
                    .ConfigureServices(((context, services) => {
                        services.AddSingleton<Server>(sc => server);
                        services.AddSingleton<Channel>(sc => channel);
                        // TODO: Find if these will work without a factory.

                        services.AddSingleton<Scynet.Subscriber.SubscriberClient>(sc => new Scynet.Subscriber.SubscriberClient(channel));
                        services.AddSingleton<Scynet.Publisher.PublisherClient>(sc => new Scynet.Publisher.PublisherClient(channel));
                        services.AddSingleton<Scynet.Hatchery.HatcheryClient>(sc => new Scynet.Hatchery.HatcheryClient(channel));
                        
                    }));

                



                var host = builder.Build();
                

                Console.WriteLine("Running...");

                await host.StartAsync();
                server.Start();

                Console.WriteLine("Yay, everything worked!");
                Console.WriteLine("\nPress Enter to terminate...\n");
                Console.ReadLine();

                await host.StopAsync();
                await server.ShutdownAsync();

                return 0;
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                return 1;
            }
        }
    }
}

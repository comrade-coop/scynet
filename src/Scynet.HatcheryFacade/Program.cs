using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using Confluent.Kafka;
using Grpc.Core;
using Grpc.Core.Interceptors;
using Kafka.Public;
using Microsoft.AspNetCore;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Orleans;
using Orleans.Configuration;
using Scynet.HatcheryFacade.RPC;
using IClusterClient = Orleans.IClusterClient;

namespace Scynet.HatcheryFacade
{
    public class Program
    {
        public static void Main(string[] args)
        {
            CreateWebHostBuilder(args).Build().Run();
        }

        public static async Task<IClusterClient> ConnectClient()
        {
            Console.WriteLine("Configuring connection to local silo...");

            var builder = new ClientBuilder()
                .UseLocalhostClustering(30000)
                .Configure<ClusterOptions>(options =>
                {
                    options.ClusterId = "dev";
                    options.ServiceId = "Scynet";
                })
                .ConfigureLogging(logging => logging.AddConsole());

            IClusterClient client = builder.Build();
            Console.WriteLine("Connecting...");

            await client.Connect();

            Console.WriteLine("Client successfully connected to silo host");

            return client;
        }

        public static IWebHostBuilder CreateWebHostBuilder(string[] args)
        {

            return WebHost.CreateDefaultBuilder(args)
                .ConfigureAppConfiguration((hostingContext, config) =>
                {
                    config.AddJsonFile("appsettings.json", optional: false, reloadOnChange: false);
                })
                .ConfigureServices(services =>
                {

                    services.AddSingleton<IClusterClient>(sp => ConnectClient().Result);
                    services.AddSingleton<RPC.HatcheryFacade, RPC.HatcheryFacade>();
                    services.AddSingleton<SubscriberFacade, SubscriberFacade>();
                    services.AddSingleton<IHostedService, SubscriberClient>();
                    services.AddSingleton<LoggingInterceptor, LoggingInterceptor>();
                    // TODO: Find a better way to indicate that these are brokers. OR
                    // TODO: Load brokers from appsettings.json
                    services.AddSingleton<IEnumerable<string>>(sp => new List<string>() { "127.0.0.1:9092" });
                    services.AddSingleton<IEnumerable<Server>>(sp =>
                    {
                        var hatcheryService = Hatchery.BindService(sp.GetService<RPC.HatcheryFacade>());
                        var subscriberService = Subscriber.BindService(sp.GetService<SubscriberFacade>());
                        var loggingInterceptor = sp.GetRequiredService<LoggingInterceptor>();
                        return new List<Server>()
                        {
                            new Server()
                            {
                                Services =
                                {
                                    hatcheryService.Intercept(loggingInterceptor),
                                    subscriberService.Intercept(loggingInterceptor),
                                },
                                Ports = {new ServerPort("0.0.0.0", 9998, ServerCredentials.Insecure)}
                            }
                        };
                    });
                    services.AddSingleton<IHostedService, GrpcBackgroundService>();
                })
                .UseStartup<Startup>();
        }
    }
}

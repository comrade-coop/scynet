using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using Grpc.Core;
using Grpc.Core.Interceptors;
using Microsoft.AspNetCore;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Orleans;
using Orleans.Configuration;
using Scynet.HatcheryFacade.RPC;

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
                .UseLocalhostClustering()
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

        public static IWebHostBuilder CreateWebHostBuilder(string[] args) {
            var service = Hatchery.BindService(new HatcheryService());

            return WebHost.CreateDefaultBuilder(args)
                .ConfigureServices(services =>
                {
                    services.AddSingleton<IClusterClient>(sp => ConnectClient().Result);
                    services.AddSingleton<IEnumerable<Server>>(sp => new List<Server>()
                    {
                        new Server()
                        {
                            Services = { service.Intercept(new LoggingInterceptor(sp.GetRequiredService<ILogger<LoggingInterceptor>>())) },
                            Ports = { new ServerPort("0.0.0.0", 9998, ServerCredentials.Insecure)}
                        }
                    });
                    services.AddSingleton<IHostedService, GrpcBackgroundService>();
                })
                .UseStartup<Startup>();
        }
    }
}

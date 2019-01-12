using System;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Orleans;
using Orleans.Configuration;
using Orleans.Hosting;
using Scynet.Grains;

namespace Scynet.LocalSilo
{
    public class Program
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

                var builder = new SiloHostBuilder()
                    .UseLocalhostClustering()
                    .Configure<ClusterOptions>(options => {
                        options.ClusterId = "dev";
                        options.ServiceId = "Scynet";
                    })
                    .ConfigureApplicationParts(parts => parts
                        .AddApplicationPart(typeof(ComponentAgent).Assembly)
                        .WithReferences())
                    .ConfigureLogging(logging => logging.AddConsole());

                var host = builder.Build();

                Console.WriteLine("Running...");

                await host.StartAsync();

                Console.WriteLine("Yay, everything worked!");
                Console.WriteLine("\nPress Enter to terminate...\n");
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
    }
}

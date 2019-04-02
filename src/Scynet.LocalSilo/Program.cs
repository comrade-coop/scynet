using System;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Orleans;
using Orleans.Configuration;
using Orleans.Hosting;
using Scynet.Grains;
using Scynet.GrainInterfaces.Component;

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
                    .UseLocalhostClustering(11111, 30000)
                    .Configure<ClusterOptions>(options =>
                    {
                        options.ClusterId = "dev";
                        options.ServiceId = "Scynet";
                    })
                    .ConfigureApplicationParts(parts => parts
                        .AddApplicationPart(typeof(Component).Assembly) // Any known Grain class, so it includes the whole assembly
                        .WithReferences())
                    .ConfigureLogging(logging => logging.AddConsole())
                    .UseInMemoryReminderService()
                    .AddMemoryGrainStorage("Default");

                var host = builder.Build();

                Console.WriteLine("Running...");

                await host.StartAsync();


                Console.WriteLine("Yay, everything worked!");
                Console.WriteLine("\nPress CTRL+C to shut down...\n");

                var interruptted = new TaskCompletionSource<bool>();
                Console.CancelKeyPress += (s, e) =>
                {
                    interruptted.TrySetResult(true);
                };
                await interruptted.Task;

                Console.WriteLine("Stopping...");

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

using System;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Orleans;
using Orleans.Configuration;
using Orleans.Hosting;

namespace BlockChainExtractor
{
    class Program
    {
        static async Task Main(string[] args)
        {
            var builder = new ClientBuilder()
                .UseLocalhostClustering()
                .Configure<ClusterOptions>(options =>
                {
                    options.ClusterId = "dev";
                    options.ServiceId = "EthereumBlockChainHarvester";
                })
                .AddSimpleMessageStreamProvider("SMSProvider")
                .ConfigureLogging(logging => logging.AddConsole());

            IClusterClient client = builder.Build();

            await Task.Delay(100);
            Console.WriteLine("Connecting...");

            await client.Connect();

            
        }
    }
}

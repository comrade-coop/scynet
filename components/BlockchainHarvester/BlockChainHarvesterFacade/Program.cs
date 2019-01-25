using Orleans;
using Orleans.Configuration;
using System;
using System.Threading.Tasks;
using BlockchainHarvester;
using BlockchainHarvester.GrainInterfaces;
using Grpc.Core;
using Microsoft.Extensions.Logging;
using Orleans.Hosting;
using Orleans.Streams;

namespace BlockChainHarvesterFacade
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

            var streamProvider = client.GetStreamProvider("SMSProvider");
           
            var stream = streamProvider.GetStream<byte[]>(Guid.Parse("db260371-c425-41df-b729-f530c2367bb5"), "hello");
            await stream.SubscribeAsync<byte[]>(async (data, token) => { Console.WriteLine(BitConverter.ToString(data)); });
            var extractor = client.GetGrain<IBlockChainExtractor>(0);

            await extractor.DoWork();
            Console.WriteLine("Job's done");
            await extractor.DoWork();
            Console.WriteLine("Job's done");

            await extractor.DoWork();
            Console.WriteLine("Job's done");

            await extractor.DoWork();
            Console.WriteLine("Job's done");


            var server = new Server()
            {
                Services =
                {
                    Scynet.Component.BindService(new ComponentFacade())
                },
                Ports = { new ServerPort("0.0.0.0", 0, ServerCredentials.Insecure) }
            };

            Console.WriteLine("Starting facade...");
            server.Start();

            Console.WriteLine("Yay, everything worked!");
            Console.WriteLine("\nPress Enter to terminate...\n");
            Console.ReadLine();

            await server.ShutdownAsync();
        }
    }
}


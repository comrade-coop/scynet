using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Confluent.Kafka;
using Grpc.Core;
using Kafka.Public;
using Microsoft.Extensions.Caching.Memory;
using Microsoft.Extensions.Logging;

namespace Scynet.HatcheryFacade.RPC
{
    public class PublisherFacade : Publisher.PublisherBase
    {
        private readonly ILogger<PublisherFacade> _logger;
        private MemoryCache _cache = new MemoryCache(new MemoryCacheOptions());

        public PublisherFacade(ILogger<PublisherFacade> logger)
        {
            _logger = logger;
        }

        public override async Task<PublishResponse> Publish(PublishRequest request, ServerCallContext context)
        {
            var producer = _cache.GetOrCreate<Producer<string, byte[]>>(request.AgentId,
                (factory) => new Producer<string, byte[]>(new Handle())); // TODO: Create the topic with the needed configuration.

            foreach (var message in request.Message)
            {
                await producer.ProduceAsync(request.AgentId, new Message<string, byte[]>() { Key = message.PartitionKey, Value = message.Data.ToByteArray(), Timestamp = new Timestamp((long)message.Key, TimestampType.CreateTime) }, context.CancellationToken);
            }





            return new PublishResponse() { };
        }
    }
}

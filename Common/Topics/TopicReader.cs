using Microsoft.ServiceFabric.Services.Client;
using Microsoft.ServiceFabric.Services.Remoting.Client;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Common.Topics.Service;

namespace Common.Topics
{
    class TopicReader<T>
    {
        private readonly ITopicReaderService service;
        private readonly string topic;
        public TopicReader(Uri serviceAddress, string topic)
        {
            // TODO: Use better hashing
            var value = MD5.Create().ComputeHash(Encoding.ASCII.GetBytes(topic));
            var key = BitConverter.ToInt64(value, 0);
            var partitionKey = new ServicePartitionKey(key);
            service = ServiceProxy.Create<ITopicReaderService>(serviceAddress, partitionKey, listenerName: "Read");
            this.topic = topic;
        }

        public async Task<T> ReadKey(long key)
        {
            var data = await service.ReadKey(topic, key);
            // TODO: Deserialize data to T
            throw new NotImplementedException();
            //return T
        }

        public async Task<List<T>> ReadKeyRange(long start, long end)
        {
            var data = await service.ReadKeyRange(topic, start, end);
            // TODO: Deserialize data to List<T>
            throw new NotImplementedException();
            //return new List<T>();
        }

        public async Task<T> ReadLatest()
        {
            var data = await service.ReadLatest(topic);
            // TODO: Deserialize data to T
            throw new NotImplementedException();
        }

        public void RegisterConsumerService()
        {
            throw new NotImplementedException();
        }

        public Task<long> GetLastReadKey()
        {
            throw new NotImplementedException();
        }
    }
}
 
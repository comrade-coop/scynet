using Common.Topics.Service;
using Microsoft.ServiceFabric.Services.Client;
using Microsoft.ServiceFabric.Services.Remoting.Client;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;

namespace Common.Topics
{
    class TopicWriter<T>
    {
        private readonly ITopicWriterService service;
        private readonly string topic;
        public TopicWriter(Uri serviceAddress, string topic)
        {
            // TODO: Use better hashing
            var value = MD5.Create().ComputeHash(Encoding.ASCII.GetBytes(topic));
            var key = BitConverter.ToInt64(value, 0);
            var partitionKey = new ServicePartitionKey(key);
            service = ServiceProxy.Create<ITopicWriterService>(serviceAddress, partitionKey, listenerName: "Write");
            this.topic = topic;
        }

        public async Task Write(long key, T data)
        {
            // TODO: Serialize T to byte[]
            throw new NotImplementedException();
            await service.Write(topic, key, new byte[0]);
        }
    }
}

using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;
using Orleans.Streams;

namespace BlockChainHarvesterFacade.HatcheryStreams
{
    class HatcheryAdapter: IQueueAdapter
    {
        public Task QueueMessageBatchAsync<T>(Guid streamGuid, string streamNamespace, IEnumerable<T> events, StreamSequenceToken token,
            Dictionary<string, object> requestContext)
        {
            throw new NotImplementedException();
        }

        public IQueueAdapterReceiver CreateReceiver(QueueId queueId)
        {
            throw new NotImplementedException();
        }

        public string Name { get; } = "";
        public bool IsRewindable { get; } = true;
        public StreamProviderDirection Direction { get; } = StreamProviderDirection.ReadWrite;
    }
}

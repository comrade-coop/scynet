using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Orleans.Streams;

namespace BlockChainHarvesterFacade.HatcheryStreams
{
    public class HatcheryAdapterReceiver : IQueueAdapterReceiver
    {
        public Task Initialize(TimeSpan timeout)
        {
            throw new NotImplementedException();
        }

        public Task<IList<IBatchContainer>> GetQueueMessagesAsync(int maxCount)
        {
            throw new NotImplementedException();
        }

        public Task MessagesDeliveredAsync(IList<IBatchContainer> messages)
        {
            throw new NotImplementedException();
        }

        public Task Shutdown(TimeSpan timeout)
        {
            throw new NotImplementedException();
        }
    }
}

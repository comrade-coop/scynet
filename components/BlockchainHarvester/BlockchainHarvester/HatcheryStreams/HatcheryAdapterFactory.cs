using System.Threading.Tasks;
using Orleans.Streams;

namespace BlockChainHarvesterFacade.HatcheryStreams
{
    public class HatcheryAdapterFactory: IQueueAdapterFactory
    {
        public Task<IQueueAdapter> CreateAdapter()
        {
            throw new System.NotImplementedException();
        }

        public IQueueAdapterCache GetQueueAdapterCache()
        {
            throw new System.NotImplementedException();
        }

        public IStreamQueueMapper GetStreamQueueMapper()
        {
            throw new System.NotImplementedException();
        }

        public Task<IStreamFailureHandler> GetDeliveryFailureHandler(QueueId queueId)
        {
            throw new System.NotImplementedException();
        }
    }
}

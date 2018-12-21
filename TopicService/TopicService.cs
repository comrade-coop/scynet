using System;
using System.Collections.Generic;
using System.Fabric;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.ServiceFabric.Data.Collections;
using Microsoft.ServiceFabric.Services.Communication.Runtime;
using Microsoft.ServiceFabric.Services.Runtime;
using Microsoft.ServiceFabric.Services.Remoting.Runtime;
using Microsoft.ServiceFabric.Services.Remoting;
using Common.Messaging;
using Common.Messaging.Service;

namespace TopicService
{
    /// <summary>
    /// An instance of this class is created for each service replica by the Service Fabric runtime.
    /// </summary>
    /// 
    interface ITopicWriterService
    {
        Task Write(long key, Byte[] data);
    }

    interface ITopicReaderService
    {
        Task<Byte[]> ReadKey();
        Task<List<Byte[]>> ReadKeyRange(long start, long end);
        Task<List<Byte[]>> ReadLatest();
     
    }

    internal sealed class TopicService : StatefulService, IServiceMessenger, ITopicReaderService, ITopicWriterService
    {
        private MessengerServiceImplementation messengerService;
        public TopicService(StatefulServiceContext context) : base(context)
        {
            messengerService = new MessengerServiceImplementation(this.StateManager);
        }

        #region ServiceMessenger
        public Task AddSubscription(string channel, SubscriberReference subscriber)
        {
            return messengerService.AddSubscription(channel, subscriber);
        }

        public Task ReceiveMessage(SubscriptionMessage message)
        {
            return messengerService.ReceiveMessage(message);

        }

        public Task RemoveSubscription(string channel, SubscriberReference subscriber)
        {
            return messengerService.RemoveSubscription(channel, subscriber);

        }
        #endregion

        #region TopicReader
        public Task<byte[]> ReadKey()
        {
            throw new NotImplementedException();
        }

        public Task<List<byte[]>> ReadKeyRange(long start, long end)
        {
            throw new NotImplementedException();
        }

        public Task<List<byte[]>> ReadLatest()
        {
            throw new NotImplementedException();
        }
        #endregion

        #region TopicWriter
        public Task Write(long key, byte[] data)
        {
            throw new NotImplementedException();
        }
        #endregion

        /// <summary>
        /// Optional override to create listeners (e.g., HTTP, Service Remoting, WCF, etc.) for this service replica to handle client or user requests.
        /// </summary>
        /// <remarks>
        /// For more information on service communication, see https://aka.ms/servicefabricservicecommunication
        /// </remarks>
        /// <returns>A collection of listeners.</returns>
        protected override IEnumerable<ServiceReplicaListener> CreateServiceReplicaListeners()
        {
            return this.CreateServiceRemotingReplicaListeners();
        }
    }
}

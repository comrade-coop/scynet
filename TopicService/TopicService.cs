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
using Common.Topics.Service;
using Microsoft.ServiceFabric.Services.Remoting.V2.FabricTransport.Runtime;

namespace TopicService
{
    /// <summary>
    /// An instance of this class is created for each service replica by the Service Fabric runtime.
    /// </summary>
    /// 
    internal sealed class TopicService : StatefulService, ITopicReaderService, ITopicWriterService
    {
        private MessengerServiceImplementation messengerService;
        public TopicService(StatefulServiceContext context) : base(context)
        {
            messengerService = new MessengerServiceImplementation(this.StateManager);
        }

        #region ServiceMessenger
        public Task AddSubscription(string topic, SubscriberReference subscriber)
        {
            return messengerService.AddSubscription(topic, subscriber);
        }

        public Task ReceiveMessage(SubscriptionMessage message)
        {
            return messengerService.ReceiveMessage(message);
        }

        public Task RemoveSubscription(string topic, SubscriberReference subscriber)
        {
            return messengerService.RemoveSubscription(topic, subscriber);
        }
        #endregion

        #region TopicReader
        public Task<byte[]> ReadKey(string topic, long key)
        {
            throw new NotImplementedException();
        }

        public Task<List<byte[]>> ReadKeyRange(string topic, long start, long end)
        {
            throw new NotImplementedException();
        }

        public Task<List<byte[]>> ReadLatest(string topic)
        {
            throw new NotImplementedException();
        }

        public Task<long> GetLastReadKey(string topic)
        {
            throw new NotImplementedException();
        }

        #endregion

        #region TopicWriter
        public Task Write(string topic, long key, byte[] data)
        {
            // TODO: Check that we are on the primry replica.
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
            return new[] {
                new ServiceReplicaListener(context =>
                    new FabricTransportServiceRemotingListener(context, this),
                        "Read", true),
                new ServiceReplicaListener(context =>
                    new FabricTransportServiceRemotingListener(context, this),
                        "Write", false)
            };
        }
    }
}

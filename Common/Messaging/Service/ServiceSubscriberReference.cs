using System;
using System.Fabric;
using System.Reflection;
using System.Runtime.Serialization;
using System.Threading.Tasks;
using Microsoft.ServiceFabric.Services.Client;
using Microsoft.ServiceFabric.Services.Remoting;
using Microsoft.ServiceFabric.Services.Remoting.Client;
using Microsoft.ServiceFabric.Services.Remoting.FabricTransport;
using Microsoft.ServiceFabric.Services.Runtime;

[assembly: FabricTransportServiceRemotingProvider(RemotingListenerVersion = RemotingListenerVersion.V2, RemotingClientVersion = RemotingClientVersion.V2)]
namespace Common.Messaging.Service
{
    [DataContract]
    public class ServiceSubscriberReference : SubscriberReference
    {
        [DataMember]
        public override string Id { get; set; }

        [DataMember]
        public Uri ServiceUri { get; set; }

        [DataMember]
        public long PartitionLowKey { get; set; }

        [DataMember]
        public Guid PartitionId { get; set; }

        private IServiceMessenger client;

        //For the serializer, parameterless constructor
        private ServiceSubscriberReference()
        {
        }

        public ServiceSubscriberReference(StatefulServiceBase service)
        {
            ServiceUri = service.Context.ServiceName;

            var servicePartition = (IStatefulServicePartition)service
                .GetType()
                .GetProperty("Partition", BindingFlags.Instance | BindingFlags.NonPublic) // Look for this.Partition of the Service
                .GetValue(service);
            var partitionInformation = servicePartition.PartitionInfo as Int64RangePartitionInformation;
            // We assume that we use default ranged partitioning Int64RangePartition. Implement other partitionings if we ever need to use them.

            PartitionLowKey = partitionInformation.LowKey;
            PartitionId = partitionInformation.Id;

            Id = "S" + ServiceUri + "|" + PartitionId;
        }

        public override Task Trigger(SubscriptionMessage message)
        {
            if (this.client == null)
            {
                var partitionKey = new ServicePartitionKey(PartitionLowKey);
                this.client = ServiceProxy.Create<IServiceMessenger>(ServiceUri, partitionKey);
            }

            return this.client.ReceiveMessage(message);
        }
    }
}
using System;
using System.Runtime.Serialization;
using System.Threading.Tasks;
using Microsoft.ServiceFabric.Actors;
using Microsoft.ServiceFabric.Actors.Client;
using Microsoft.ServiceFabric.Actors.Remoting.FabricTransport;
using Microsoft.ServiceFabric.Actors.Runtime;
using Microsoft.ServiceFabric.Services.Remoting;

[assembly: FabricTransportActorRemotingProvider(RemotingListenerVersion = RemotingListenerVersion.V2, RemotingClientVersion = RemotingClientVersion.V2)]
namespace Common.Messaging.Actor
{
    [DataContract]
    public class ActorSubscriberReference : SubscriberReference
    {
        [DataMember]
        public override string Id { get; set; }

        [DataMember]
        public Uri ServiceUri { get; set; }

        [DataMember]
        public ActorId ActorId { get; set; }

        private IActorMessenger client;

        private ActorSubscriberReference()
        {
        }

        public ActorSubscriberReference(ActorBase subscriber)
        {
            ServiceUri = subscriber.ServiceUri;
            ActorId = subscriber.Id;

            Id = "A" + ServiceUri + "|" + ActorId;
        }

        public override Task Trigger(SubscriptionMessage message)
        {
            if (this.client == null)
            {
                this.client = ActorProxy.Create<IActorMessenger>(ActorId, ServiceUri);
            }

            return this.client.ReceiveMessage(message);
        }
    }
}
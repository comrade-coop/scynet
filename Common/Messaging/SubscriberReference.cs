using System.Runtime.Serialization;
using System.Threading.Tasks;
using Common.Messaging.Actor;
using Common.Messaging.Service;
using Microsoft.ServiceFabric.Actors.Runtime;
using Microsoft.ServiceFabric.Services.Runtime;

namespace Common.Messaging
{
    [DataContract]
    [KnownType(typeof(ActorSubscriberReference))]
    [KnownType(typeof(ServiceSubscriberReference))]
    public abstract class SubscriberReference
    {
        [DataMember]
        public abstract string Id { get; set; }

        public abstract Task Trigger(SubscriptionMessage message);

        public static SubscriberReference GetSubscriberReference(object subscriber)
        {
            SubscriberReference result = null;

            if (subscriber is StatefulServiceBase serviceBase)
            {
                result = new ServiceSubscriberReference(serviceBase);
            }
            else if (subscriber is ActorBase actorBase)
            {
                result = new ActorSubscriberReference(actorBase);
            }

            return result;
        }
    }
}
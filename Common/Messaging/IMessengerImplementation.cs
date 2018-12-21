using System.Threading.Tasks;

namespace Common.Messaging
{
    public interface IMessengerImplementation
    {
        Task AddSubscription(string topic, SubscriberReference subscriber);
        Task RemoveSubscription(string topic, SubscriberReference subscriber);
        Task ReceiveMessage(SubscriptionMessage message);
        Task PublishMessage(SubscriptionMessage message);
    }
}

using System.Threading.Tasks;

namespace Common.Messaging
{
    public interface IMessengerImplementation
    {
        Task AddSubscription(string channel, SubscriberReference subscriber);
        Task RemoveSubscription(string channel, SubscriberReference subscriber);
        Task ReceiveMessage(SubscriptionMessage message);
        Task PublishMessage(SubscriptionMessage message);
    }
}

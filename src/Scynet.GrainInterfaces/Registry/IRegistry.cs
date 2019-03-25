using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Serialize.Linq.Nodes;

namespace Scynet.GrainInterfaces.Registry
{
    /// <summary>
    /// Registry holding queryable information about something
    /// </summary>
    /// <typeparam name="T">Type of information to hold</typeparam>
    /// <seealso cref="Scynet.GrainInterfaces.Registry.RegistryExtensions"/>
    public interface IRegistry<K, T> : Orleans.IGrainWithIntegerKey
    {
        /// <summary>
        /// Register agent/component.
        /// </summary>
        /// <param name="key">Key of the thing to be registred.</param>
        /// <param name="info">Information about the type to be registered.</param>
        Task Register(K key, T info);

        /// <summary>
        /// Get element from the registry by key
        /// </summary>
        /// <param name="key">Key of the thing requested.</param>
        Task<T> Get(K key);

        /// <summary>
        /// Query information from the registry
        /// </summary>
        /// <typeparam name="U">Result type</typeparam>
        /// <param name="expression">An ExpressionNode wrapping an Expression < Func < IEnumerable < KeyValuePair< K, T > >, U > > </param>
        /// <seealso cref="Scynet.GrainInterfaces.Registry.RegistryExtensions.Query"/>
        Task<U> QueryValue<U>(ExpressionNode expression);

        /// <summary>
        /// Query information from the registry
        /// </summary>
        /// <typeparam name="U">Type of elements of the result</typeparam>
        /// <param name="expression">An ExpressionNode wrapping an Expression < Func < IEnumerable < KeyValuePair< K, T > >, IEnumerable < U > > > </param>
        /// <seealso cref="Scynet.GrainInterfaces.Registry.RegistryExtensions.Query"/>
        Task<IEnumerable<U>> QueryCollection<U>(ExpressionNode expression);

        /// <summary>
        /// Listen for new registrations to the registry. If a subscription already exists, it will just be updated with the new expression.
        /// </summary>
        /// <param name="expression">An ExpressionNode wrapping an Expression < Func < KeyValuePair< K, T > , bool > > </param>
        /// <param name="listener">The listener which would receive the notifications</param>
        /// <param name="ref">A reference with which the subscription is tracked</param>
        Task Subscribe(ExpressionNode expression, IRegistryListener<K, T> listener, String @ref = "", SubscriptionOptions options = SubscriptionOptions.Default);

        /// <summary>
        /// Stop listening for new registrations to the registry.
        /// </summary>
        /// <param name="listener">The listener has to be unsubscribed</param>
        /// <param name="ref">A reference with which the subscription is tracked</param>
        Task Unsubscribe(IRegistryListener<K, T> listener, String @ref = "");

        // Task Renew(IRegistryListener<K, T> listener);
    }

    public enum SubscriptionOptions
    {
        AllRegistrations,
        OnlyNew,

        Default = AllRegistrations
    }
}

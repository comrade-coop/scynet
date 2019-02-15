using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;
using System.Linq;
using System.Linq.Expressions;
using Serialize.Linq.Nodes;
using Serialize.Linq.Extensions;

namespace Scynet.GrainInterfaces
{
    /// <summary>
    /// Registry holding queryable information about something
    /// </summary>
    /// <typeparam name="T">Type of information to hold</typeparam>
    /// <seealso cref="Scynet.GrainInterfaces.RegistryExtensions"/>
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
        /// <param name="key">Key of the thing registred.</param>
        /// <seealso cref="Scynet.GrainInterfaces.RegistryExtensions.Query"/>
        Task<T> Get(K key);

        /// <summary>
        /// Query information from the registry
        /// </summary>
        /// <typeparam name="U">Result type</typeparam>
        /// <param name="expression">An ExpressionNode wrapping an Expression < Func < IEnumerable < KeyValuePair< K, T > >, U > > </param>
        /// <seealso cref="Scynet.GrainInterfaces.RegistryExtensions.Query"/>
        Task<U> QueryValue<U>(ExpressionNode expression);

        /// <summary>
        /// Query information from the registry
        /// </summary>
        /// <typeparam name="U">Type of elements of the result</typeparam>
        /// <param name="expression">An ExpressionNode wrapping an Expression < Func < IEnumerable < KeyValuePair< K, T > >, IEnumerable < U > > > </param>
        /// <seealso cref="Scynet.GrainInterfaces.RegistryExtensions.Query"/>
        Task<IEnumerable<U>> QueryCollection<U>(ExpressionNode expression);

        /// <summary>
        /// Listen for new registrations to the registry. If a subscription already exists, it will just be updated with the new expression.
        /// </summary>
        /// <param name="expression">An ExpressionNode wrapping an Expression < Func < KeyValuePair< K, T > , bool > > </param>
        /// <param name="listener">The listener which would receive the notifications</param>
        /// <param name="ref">A reference with which the subscription is tracked</param>
        Task Subscribe(ExpressionNode expression, IRegistryListener<K, T> listener, String @ref = "");

        /// <summary>
        /// Stop listening for new registrations to the registry.
        /// </summary>
        /// <param name="listener">The listener has to be unsubscribed</param>
        /// <param name="ref">A reference with which the subscription is tracked</param>
        Task Unsubscribe(IRegistryListener<K, T> listener, String @ref = "");

        // Task Renew(IRegistryListener<K, T> listener);
    }

    public static class RegistryExtensions
    {
        /// <summary>
        /// Query information from the registry. This is a wrapper which can simplify work with <see cref="Scynet.GrainInterfaces.IRegistry.QueryCollection"/>
        /// </summary>
        /// <typeparam name="K">Registry key type</typeparam>
        /// <typeparam name="T">Registry value type</typeparam>
        /// <typeparam name="U">Type of result item</typeparam>
        /// <param name="query">A lambda which processes the Registry's information</param>
        public static Task<IEnumerable<U>> Query<K, T, U>(this IRegistry<K, T> registry, Expression<Func<IEnumerable<KeyValuePair<K, T>>, IEnumerable<U>>> query)
        {
            var expressionNode = query.ToExpressionNode();
            return registry.QueryCollection<U>(expressionNode);
        }

        /// <summary>
        /// Query information from the registry. This is a wrapper which can simplify work with <see cref="Scynet.GrainInterfaces.IRegistry.QueryRaw"/>
        /// </summary>
        /// <typeparam name="K">Registry key type</typeparam>
        /// <typeparam name="T">Registry value type</typeparam>
        /// <typeparam name="U">Result type</typeparam>
        /// <param name="query">A lambda which processes the Registry's information</param>
        public static Task<U> Query<K, T, U>(this IRegistry<K, T> registry, Expression<Func<IEnumerable<KeyValuePair<K, T>>, U>> query)
        {
            var expressionNode = query.ToExpressionNode();
            return registry.QueryValue<U>(expressionNode);
        }

        /// <summary>
        /// Listen for new registrations to the registry. If a subscription already exists, it will just be updated with the new expression. This is a wrapper which can simplify work with <see cref="Scynet.GrainInterfaces.IRegistry.Subscribe"/>
        /// </summary>
        /// <typeparam name="K">Registry key type</typeparam>
        /// <typeparam name="T">Registry value type</typeparam>
        /// <param name="query">A lambda which decides what items are interesting for the listener</param>
        /// <param name="listener">The listener which would receive the notifications</param>
        /// <param name="ref">A reference with which the subscription is tracked</param>
        public static Task Subscribe<K, T>(this IRegistry<K, T> registry, Expression<Func<KeyValuePair<K, T>, bool>> query, IRegistryListener<K, T> listener, String @ref = "")
        {
            var expressionNode = query.ToExpressionNode();
            return registry.Subscribe(expressionNode, listener, @ref);
        }
    }
}

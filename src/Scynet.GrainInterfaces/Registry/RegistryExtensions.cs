using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Linq;
using System.Linq.Expressions;
using Serialize.Linq.Nodes;
using Serialize.Linq.Extensions;

namespace Scynet.GrainInterfaces.Registry
{
    /// <summary>
    /// Helper for working with IRegistry.
    /// </summary>
    /// <seealso cref="Scynet.GrainInterfaces.Registry.IRegistry"/>
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
        public static Task Subscribe<K, T>(this IRegistry<K, T> registry, Expression<Func<K, T, bool>> query, IRegistryListener<K, T> listener, String @ref = "", SubscriptionOptions options = SubscriptionOptions.Default)
        {
            var expressionNode = query.ToExpressionNode();
            return registry.Subscribe(expressionNode, listener, @ref, options);
        }
    }
}

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
    public interface IRegistry<T> : Orleans.IGrainWithIntegerKey
    {
        /// <summary>
        /// Register agent/component.
        /// </summary>
        /// <param name="info">Information about the type to be registered.</param>
        Task Register(T info);

        /// <summary>
        /// Query information from the registry
        /// </summary>
        /// <typeparam name="K">Result type</typeparam>
        /// <param name="expression">An ExpressionNode wrapping an Expression < Func < List < T >, K > > </param>
        /// <seealso cref="Scynet.GrainInterfaces.RegistryExtensions.Query"/>
        Task<K> QueryValue<K>(ExpressionNode expression);

        /// <summary>
        /// Query information from the registry
        /// </summary>
        /// <typeparam name="K">Type of elements of the result</typeparam>
        /// <param name="expression">An ExpressionNode wrapping an Expression < Func < List < T >, IEnumerable < K > > > </param>
        /// <seealso cref="Scynet.GrainInterfaces.RegistryExtensions.Query"/>
        Task<IEnumerable<K>> QueryCollection<K>(ExpressionNode expression);

        /// <summary>
        /// Listen for new registrations to the registry. If a subscription already exists, it will just be updated with the new expression.
        /// </summary>
        /// <param name="expression">An ExpressionNode wrapping an Expression < Func < T , bool > > </param>
        /// <param name="listener">The listener which would receive the notifications</param>
        /// <param name="ref">A reference with which the subscription is tracked</param>
        Task Subscribe(ExpressionNode expression, IRegistryListener<T> listener, String @ref = "");

        /// <summary>
        /// Stop listening for new registrations to the registry.
        /// </summary>
        /// <param name="listener">The listener has to be unsubscribed</param>
        /// <param name="ref">A reference with which the subscription is tracked</param>
        Task Unsubscribe(IRegistryListener<T> listener, String @ref = "");

        // Task Renew(IRegistryListener<T> listener);
    }

    public static class RegistryExtensions
    {
        /// <summary>
        /// Query information from the registry. This is a wrapper which can simplify work with <see cref="Scynet.GrainInterfaces.IRegistry.QueryCollection"/>
        /// </summary>
        /// <typeparam name="T">Registry type</typeparam>
        /// <typeparam name="K">Type of result item</typeparam>
        /// <param name="query">A lambda which processes the Registry's information</param>
        public static Task<IEnumerable<K>> Query<T, K>(this IRegistry<T> registry, Expression<Func<IEnumerable<T>, IEnumerable<K>>> query)
        {
            var expressionNode = query.ToExpressionNode();
            return registry.QueryCollection<K>(expressionNode);
        }

        /// <summary>
        /// Query information from the registry. This is a wrapper which can simplify work with <see cref="Scynet.GrainInterfaces.IRegistry.QueryRaw"/>
        /// </summary>
        /// <typeparam name="T">Registry type</typeparam>
        /// <typeparam name="K">Result type</typeparam>
        /// <param name="query">A lambda which processes the Registry's information</param>
        public static Task<K> Query<T, K>(this IRegistry<T> registry, Expression<Func<IEnumerable<T>, K>> query)
        {
            var expressionNode = query.ToExpressionNode();
            return registry.QueryValue<K>(expressionNode);
        }

        /// <summary>
        /// Listen for new registrations to the registry. If a subscription already exists, it will just be updated with the new expression. This is a wrapper which can simplify work with <see cref="Scynet.GrainInterfaces.IRegistry.Subscribe"/>
        /// </summary>
        /// <param name="query">A lambda which decides what items are interesting for the listener</param>
        /// <param name="listener">The listener which would receive the notifications</param>
        /// <param name="ref">A reference with which the subscription is tracked</param>
        public static Task Subscribe<T>(this IRegistry<T> registry, Expression<Func<T, bool>> query, IRegistryListener<T> listener, String @ref = "")
        {
            var expressionNode = query.ToExpressionNode();
            return registry.Subscribe(expressionNode, listener, @ref);
        }
    }
}

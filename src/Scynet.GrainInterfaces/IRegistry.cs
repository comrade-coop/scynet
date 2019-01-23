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
    public interface IRegistry<Т> : Orleans.IGrainWithIntegerKey
    {
        /// <summary>
        /// Register agent/component.
        /// </summary>
        /// <param name="info">Information about the type to be registered.</param>
        Task Register(Т info);

        /// <summary>
        /// Query information from the registry
        /// </summary>
        /// <typeparam name="K">Result type</typeparam>
        /// <param name="expression">An ExpressionNode wrapping an Expression < Func < List < T >, K > > </param>
        /// <seealso cref="Scynet.GrainInterfaces.RegistryExtensions.Query"/>
        Task<K> Query<K>(ExpressionNode expression);
    }

    public static class RegistryExtensions
    {
        /// <summary>
        /// Query information from the registry. This is a wrapper which can simplify work with <see cref="Scynet.GrainInterfaces.IRegistry.Query"/>
        /// Note: If K is IEnumerable, you should be using the version of Query with one type parameter
        /// </summary>
        /// <typeparam name="T">Registry type</typeparam>
        /// <typeparam name="K">Result type</typeparam>
        /// <param name="query">A lambda which processes the Registry's information</param>
        public static Task<K> Query<T, K>(this IRegistry<T> registry, Expression<Func<IEnumerable<T>, K>> query)
        {
            var expressionNode = query.ToExpressionNode();
            return registry.Query<K>(expressionNode);
        }
    }
}

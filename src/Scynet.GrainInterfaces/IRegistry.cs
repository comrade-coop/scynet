using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;
using System.Linq;
using System.Linq.Expressions;
using Serialize.Linq.Nodes;

namespace Scynet.GrainInterfaces
{
    public interface IRegistry<Т> : Orleans.IGrainWithIntegerKey
    {
        /// <summary>
        /// Register agent/component.
        /// </summary>
        /// <param name="info">Information about the type to be registered.</param>
        Task Register(Т info);

        /// <summary>
        /// Get list of agents by LINQ.
        /// </summary>
        /// <param name="query">Query based on which agents will be returned.</param>
        Task<List<Т>> QueryAgents(ExpressionNode expression);

        Task<K> Sheny<K>(ExpressionNode expression);
    }
}

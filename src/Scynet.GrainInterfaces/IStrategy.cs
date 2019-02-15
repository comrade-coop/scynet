using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Scynet.GrainInterfaces
{
    /// <summary>
    /// A strategy
    /// </summary>
    public interface IStrategy : Orleans.IGrain
    {
        /// <summary>
        /// Register a component
        /// </summary>
        Task RegisterComponent(IComponent component);

        /// <summary>
        /// Unregister a component
        /// </summary>
        Task UnregisterComponent(IComponent component);
    }
}

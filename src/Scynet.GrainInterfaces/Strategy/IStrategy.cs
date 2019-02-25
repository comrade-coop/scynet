using System;
using System.Threading.Tasks;
using Scynet.GrainInterfaces.Component;

namespace Scynet.GrainInterfaces.Strategy
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

        /// <summary>
        /// Change the logic of the strategy
        /// </summary>
        Task SetStrategyLogic(String type, String source);
    }
}

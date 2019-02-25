using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Scynet.GrainInterfaces.Agent;

namespace Scynet.GrainInterfaces.Component
{
    /// <summary>
    /// A component
    /// </summary>
    public interface IComponent : Orleans.IGrainWithGuidKey
    {
        /// <summary>
        /// Initialize the component
        /// </summary>
        Task Initialize(String address, ISet<String> runnerTypes);

        /// <summary>
        /// Get the address of the component
        /// </summary>
        Task<String> GetAddress();

        /// <summary>
        /// Mark the component as disconnected
        /// </summary>
        Task Disconnect();

        /// <summary>
        /// Register an input for the component
        /// </summary>
        Task RegisterInput(IAgent agent);
    }
}

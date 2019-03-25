using System;
using System.Collections.Generic;
using System.Threading.Tasks;

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

        /// <summary>
        /// Start an agent (assumes said agent is runnable in the component)
        /// </summary>
        Task StartAgent(IAgent agent);

        /// <summary>
        /// Stops an agent (assumes said agent is running in the component, though it is likely harmless if it is not)
        /// </summary>
        Task StopAgent(IAgent agent);
    }
}

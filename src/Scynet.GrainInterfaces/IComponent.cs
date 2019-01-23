using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Scynet.GrainInterfaces
{
    /// <summary>
    /// A component
    /// </summary>
    public interface IComponent : Orleans.IGrainWithGuidKey
    {
        /// <summary>
        /// Initialize the component
        /// </summary>
        Task Initialize(String endpoint, String runnerType);

        /// <summary>
        /// Register an input for the component
        /// </summary>
        Task RegisterInput(Guid agentId);

        /// <summary>
        /// Create an agent, likely by registering it the AgentRegistry
        /// </summary>
        Task<Guid> CreateAgent(EggDescriptor egg);
    }
}

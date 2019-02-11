using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Scynet.GrainInterfaces
{
    /// <summary>
    /// An agent created by a component
    /// </summary>
    public interface IComponentAgent : IAgent
    {
        /// <summary>
        /// Initialize the agent
        /// </summary>
        Task Initialize(IComponent component, byte[] data);

        /// <summary>
        /// Get the component of the agent
        /// </summary>
        Task<IComponent> GetComponent();

        /// <summary>
        /// Get the data of the agent
        /// </summary>
        Task<byte[]> GetData();
    }
}

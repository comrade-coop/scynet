using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Scynet.GrainInterfaces.Agent
{
    /// <summary>
    /// An agent
    /// </summary>
    public interface IAgent : Orleans.IGrainWithGuidKey
    {
        /// <summary>
        /// List engagers of the Agent.
        /// </summary>
        Task<IEnumerable<EngagementInfo>> GetActiveEngagements();

        /// <summary>
        /// Engage the agent. You should do this prior to trying to listen to its stream.
        /// </summary>
        /// <param name="engager">Information about the engager to engage.</param>
        Task Engage(IEngager engager);

        /// <summary>
        /// Release (disengage) the agent. You should do this after finishing listening.
        /// </summary>
        /// <param name="engager">Information about the engager to release.</param>
        Task Release(IEngager engager);

        /// <summary>
        /// Get the agent's topic.
        /// </summary>
        Task<String> GetTopic();

        /// <summary>
        /// Release all engagements, also known as "hibernation".
        /// </summary>
        Task ReleaseAll();

        /// <summary>
        /// Update the agent's metadata
        /// </summary>
        Task SetMetadata(string key, string value);
    }

    public class EngagementInfo
    {
        public IEngager Engager { get; set; }
        public DateTime EngagedSince { get; set; }
    }
}

using System.Collections.Generic;
using System.Threading.Tasks;

namespace Scynet.GrainInterfaces
{
    /// <summary>
    /// An agent
    /// </summary>
    public interface IAgent : Orleans.IGrainWithGuidKey
    {
        /// <summary>
        /// List engagements of the Agent.
        /// </summary>
        Task<IEnumerable<Engagement>> GetActiveEngagements();

        /// <summary>
        /// Engage the agent. You should do this prior to trying to listen to its stream.
        /// </summary>
        /// <param name="engagement">Information about the engagement to engage.</param>
        Task Engage(Engagement engagement);

        /// <summary>
        /// Release (disengage) the agent. You should do after finishing listening.
        /// </summary>
        /// <param name="engagement">Information about the engagement to release.</param>
        Task Release(Engagement engagement);
    }
}

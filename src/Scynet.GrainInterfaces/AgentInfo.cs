using System;
using System.Collections.Generic;
using System.Text;

namespace Scynet.GrainInterfaces
{
    [Serializable]
    public class AgentInfo
    {
        public Guid ComponentId { get; set; }
        public String RunnerType { get; set; }

        public AgentInfo()
        {

        }
    }
}

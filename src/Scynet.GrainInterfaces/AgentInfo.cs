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
        public List<List<uint>> OutputShapes { get; set; }
        public uint Frequency { get; set; }
        public IAgent Agent { get; set; }

        public AgentInfo()
        {

        }
    }
}

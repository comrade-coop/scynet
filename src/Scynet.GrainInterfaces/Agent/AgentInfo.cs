using System;
using System.Collections.Generic;

namespace Scynet.GrainInterfaces.Agent
{
    [Serializable]
    public class AgentInfo
    {
        public Guid ComponentId { get; set; }
        public String RunnerType { get; set; }
        public List<List<uint>> OutputShapes { get; set; }
        public uint Frequency { get; set; }
        public uint Price { get; set; }
        public IAgent Agent { get; set; }

        public AgentInfo()
        {

        }
    }
}

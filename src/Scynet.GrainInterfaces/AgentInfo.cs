using System;
using System.Collections.Generic;
using System.Text;

namespace Scynet.GrainInterfaces
{
    [Serializable]
    public class AgentInfo
    {
        public Guid Id { get; set; }
        public String RunnerType { get; set; }
        public byte[] EggHash { get; set; }

        public AgentInfo()
        {

        }
    }
}

using System;
using System.Collections.Generic;
using System.Text;

namespace Scynet.GrainInterfaces
{
    [Serializable]
    public class AgentInfo
    {
        public Guid Id { get; set; }
        public EggDescriptor Descriptor { get; set; }

        public AgentInfo()
        {

        }
    }
}

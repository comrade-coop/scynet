using System;
using System.Collections.Generic;
using System.Text;

namespace Scynet.GrainInterfaces
{
    public class AgentInfo
    {
        public string Id { get; set; }

        public AgentInfo()
        {

        }

        public AgentInfo(string id)
        {
            Id = id;
        }
    }
}

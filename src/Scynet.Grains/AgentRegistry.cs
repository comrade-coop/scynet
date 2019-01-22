using Microsoft.Extensions.Logging;
using Scynet.GrainInterfaces;
using System;
using System.Collections.Generic;
using System.Text;

namespace Scynet.Grains
{
    public class AgentRegistry : Registry<AgentInfo>
    {
        public AgentRegistry(ILogger<AgentRegistry> logger) : base(logger)
        {

        }
    }
}

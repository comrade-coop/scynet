using Microsoft.Extensions.Logging;
using Scynet.GrainInterfaces;
using System;
using System.Collections.Generic;
using System.Text;

namespace Scynet.Grains
{
    public class ComponentRegistry : Registry<ComponentInfo>
    {
        public ComponentRegistry(ILogger<ComponentRegistry> logger) : base(logger)
        {

        }
    }
}

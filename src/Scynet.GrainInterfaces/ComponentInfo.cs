using System;
using System.Collections.Generic;
using System.Text;

namespace Scynet.GrainInterfaces
{
    [Serializable]
    public class ComponentInfo
    {
        public ISet<String> RunnerTypes { get; set; }
    }
}

using System;
using System.Collections.Generic;

namespace Scynet.GrainInterfaces.Component
{
    [Serializable]
    public class ComponentInfo
    {
        public ISet<String> RunnerTypes { get; set; }
    }
}

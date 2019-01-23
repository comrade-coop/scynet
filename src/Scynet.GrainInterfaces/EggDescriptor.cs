using System;

namespace Scynet.GrainInterfaces
{
    [Serializable]
    public class EggDescriptor
    {
        public String RunnerType { get; set; }
        public String EggHash { get; set; }
    }
}

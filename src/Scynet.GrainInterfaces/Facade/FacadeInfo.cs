using System;
using System.Collections.Generic;

namespace Scynet.GrainInterfaces.Facade
{
    [Serializable]
    public class FacadeInfo
    {
        public IFacade Facade { get; set; }
        public DateTime LastUpdate { get; set; }
    }
}

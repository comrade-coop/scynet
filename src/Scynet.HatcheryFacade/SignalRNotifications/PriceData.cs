using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace Scynet.HatcheryFacade.SignalRNotifications
{
    public class PriceData
    {
        public string Date { get; set; }
        public float Close { get; set; }
        public float High { get; set; }
        public float Low { get; set; }
        public float Open { get; set; }
        public float VolumeFrom { get; set; }
        public float VolumeTo { get; set; }
    }
}

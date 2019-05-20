using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace Scynet.HatcheryFacade.SignalRNotifications
{
    public class Prediction
    {
        public string Date { get; set; }
        public float Value { get; set; }
        public bool? IsTrue { get; set; }
    }
}

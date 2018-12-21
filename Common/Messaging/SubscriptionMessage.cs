using Newtonsoft.Json;
using System;
using System.IO;
using System.Runtime.Serialization;
using System.Xml;

namespace Common.Messaging
{
    [DataContract]
    public sealed class SubscriptionMessage: IExtensibleDataObject
    {
        public ExtensionDataObject ExtensionData { get; set; }

        [DataMember]
        public string Payload { get; set; }

        [DataMember]
        public string Channel { get; set; }

        [DataMember]
        public string SenderId { get; set; }

        [DataMember]
        public bool ShouldRetry { get; set; } //TODO: Dariel - This is currently ignored everywhere.

        public SubscriptionMessage(object payload)
        {
            var settings = new JsonSerializerSettings { TypeNameHandling = TypeNameHandling.All };
            Payload = JsonConvert.SerializeObject(payload, payload.GetType(), settings);
        }

        // TODO - Dariel: Add a static method that can fill out missing properties (i.e. Channel and SenderId) if it can find sufficient data in the payload etc
    }
}

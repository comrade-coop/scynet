using System;

namespace GrainInterfaces
{
    public class MockAgent
    {
        public string Id { get; set; }
        public string Name { get; set; }
        public bool IsRunning { get; set; }
        //contain stream for data reading

        public MockAgent(string id, string name)
        {
            this.Id = id;
            this.Name = name;
        }

        public MockAgent(MockEgg egg)
        {
            this.Id = egg.Id;
            this.Name = System.Text.Encoding.UTF8.GetString(egg.Data);
        }

        public void Start()
        {
            this.IsRunning = true;
            Console.WriteLine("Agent with id" + this.Id + " started.");
        }

        public void Stop()
        {
            this.IsRunning = false;
        }

        public bool IsStarted()
        {
            return this.IsRunning == true;
        }
    }
}

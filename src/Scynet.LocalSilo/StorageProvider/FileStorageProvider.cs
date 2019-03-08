using System;
using System.Collections.Generic;
using System.Text;
using System.Linq;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Orleans;
using Orleans.Runtime;
using Orleans.Storage;
using Orleans.Providers;
using Orleans.Serialization;
using System.Threading;

namespace Scynet.LocalSilo.StorageProvider
{
    public class FileStorageProvider : IGrainStorage, ILifecycleParticipant<ISiloLifecycle>
    {
        /// <summary>
        /// The directory path, relative to the host of the silo. Set from
        /// configuration data during initialization.
        /// </summary>
        public string RootDirectory { get; set; }

        /// <summary>
        /// Storage provider name
        /// </summary>
        public string Name { get; protected set; }

        /// <summary>
        /// Data manager instance
        /// </summary>
        /// <remarks>The data manager is responsible for reading and writing JSON strings.</remarks>
        protected IJSONStateDataManager DataManager { get; set; }

        private JsonSerializerSettings SerializerSettings;
        private readonly IGrainFactory GrainFactory;
        private readonly ITypeResolver TypeResolver;

        public FileStorageProvider(string name, string rootDir, IGrainFactory grainFactory, ITypeResolver typeResolver)
        {
            this.Name = name;
            this.RootDirectory = rootDir;
            if (string.IsNullOrWhiteSpace(RootDirectory))
            {
                throw new ArgumentException("RootDirectory property not set");
            }
            DataManager = new GrainStateFileDataManager(RootDirectory);

            this.GrainFactory = grainFactory;
            this.TypeResolver = typeResolver;
        }

        public Task ClearStateAsync(string grainType, GrainReference grainReference, IGrainState grainState)
        {
            if (DataManager == null) throw new ArgumentException("DataManager property not initialized");

            var grainTypeName = grainType.Split('.').Last();

            DataManager.Delete(grainTypeName, grainReference.ToKeyString());
            return Task.CompletedTask;
        }

        public async Task ReadStateAsync(string grainType, GrainReference grainReference, IGrainState grainState)
        {
            if (DataManager == null) throw new ArgumentException("DataManager property not initialized");

            var grainTypeName = grainType.Split('.').Last();

            var entityData = await DataManager.Read(grainTypeName, grainReference.GetUniformHashCode().ToString());
            if (entityData != null)
            {
                ConvertFromStorageFormat(grainState, entityData);
            }
        }

        public Task WriteStateAsync(string grainType, GrainReference grainReference, IGrainState grainState)
        {
            if (DataManager == null) throw new ArgumentException("DataManager property not initialized");
            var grainTypeName = grainType.Split('.').Last();
            var entityData = ConvertToStorageFormat(grainState);
            return DataManager.Write(grainTypeName, grainReference.GetUniformHashCode().ToString(), entityData);
        }

        protected string ConvertToStorageFormat(IGrainState grainState)
        {
            var serialized = JsonConvert.SerializeObject(grainState.State, this.SerializerSettings);
            return serialized;
        }

        /// <summary>
        /// Constructs a grain state instance by deserializing a JSON document.
        /// </summary>
        /// <param name="grainState">Grain state to be populated for storage.</param>
        /// <param name="entityData">JSON storage format representaiton of the grain state.</param>
        protected void ConvertFromStorageFormat(IGrainState grainState, string entityData)
        {
            var deserialized = JsonConvert.DeserializeObject(entityData, this.SerializerSettings);
            grainState.State = deserialized;
        }

        public void Participate(ISiloLifecycle lifecycle)
        {
            lifecycle.Subscribe(OptionFormattingUtilities.Name<FileStorageProvider>(this.Name),
                ServiceLifecycleStage.ApplicationServices, Init);
        }

        private Task Init(CancellationToken ct)
        {
            this.SerializerSettings = OrleansJsonSerializer.UpdateSerializerSettings(
                OrleansJsonSerializer.GetDefaultSerializerSettings(this.TypeResolver, this.GrainFactory
                ), false, true, TypeNameHandling.All);
            return Task.CompletedTask;
        }
    }
}

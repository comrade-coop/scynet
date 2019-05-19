using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using System.Threading.Tasks;
using Orleans;
using Orleans.Runtime;

namespace Scynet.LocalSilo.StorageProvider
{
    public class GrainStateFileDataManager : IJSONStateDataManager
    {
        private readonly DirectoryInfo Directory;

        /// <summary>
        /// Constructor
        /// </summary>
        /// <param name="storageDirectory">A path relative to the silo host process' working directory.</param>
        public GrainStateFileDataManager(string storageDirectory)
        {
            Directory = new DirectoryInfo(storageDirectory);
            if (!Directory.Exists)
            {
                Directory.Create();
            }
        }


        /// <summary>
        /// Deletes a file representing a grain state object.
        /// </summary>
        /// <param name="collectionName">The type of the grain state object.</param>
        /// <param name="key">The grain id string.</param>
        /// <returns>Completion promise for this operation.</returns>
        public Task Delete(string collectionName, string key)
        {
            FileInfo fileInfo = GetStorageFilePath(collectionName, key);

            if (fileInfo.Exists)
            {
                fileInfo.Delete();
            }

            return Task.CompletedTask;
        }

        /// <summary>
        /// Reads a file representing a grain state object.
        /// </summary>
        /// <param name="collectionName">The type of the grain state object.</param>
        /// <param name="key">The grain id string.</param>
        /// <returns>Completion promise for this operation.</returns>
        public async Task<string> Read(string collectionName, string key)
        {
            FileInfo fileInfo = GetStorageFilePath(collectionName, key);

            if (!fileInfo.Exists)
            {
                return null;
            }

            using (var stream = fileInfo.OpenText())
            {
                return await stream.ReadToEndAsync();
            }
        }

        /// <summary>
        /// Writes a file representing a grain state object.
        /// </summary>
        /// <param name="collectionName">The type of the grain state object.</param>
        /// <param name="key">The grain id string.</param>
        /// <param name="entityData">The grain state data to be stored./</param>
        /// <returns>Completion promise for this operation.</returns>
        public async Task Write(string collectionName, string key, string entityData)
        {
            FileInfo fileInfo = GetStorageFilePath(collectionName, key);

            using (var stream = new StreamWriter(fileInfo.Open(FileMode.Create, FileAccess.Write)))
            {
                await stream.WriteAsync(entityData);
            }
        }

        public void Dispose()
        {

        }

        private FileInfo GetStorageFilePath(string collectionName, string key)
        {
            string fileName = key + "-" + collectionName + ".json";
            //string fileName = collectionName + ".json";
            string path = Path.Combine(Directory.FullName, fileName);
            return new FileInfo(path);
        }
    }
}

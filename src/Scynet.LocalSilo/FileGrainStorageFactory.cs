using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Options;
using Orleans.Storage;
using Scynet.LocalSilo.StorageProvider;
using System;
using System.Collections.Generic;
using System.Text;

namespace Scynet.LocalSilo
{
    public static class FileGrainStorageFactory
    {
        public static IGrainStorage Create(IServiceProvider services, string name)
        {
           // IOptionsSnapshot<MinioGrainStorageOptions> optionsSnapshot = services.GetRequiredService<IOptionsSnapshot<MinioGrainStorageOptions>>();
            //var options = optionsSnapshot.Get(name);
            //IMinioStorage storage = ActivatorUtilities.CreateInstance<MinioStorage>(services, options.AccessKey, options.SecretKey, options.Endpoint);
            return ActivatorUtilities.CreateInstance<FileStorageProvider>(services, name, "./filestoragefolder");
        }
    }
}

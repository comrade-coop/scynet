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
            IOptionsSnapshot<FileStorageOptions> optionsSnapshot = services.GetRequiredService<IOptionsSnapshot<FileStorageOptions>>();
            var options = optionsSnapshot.Get(name);
            return ActivatorUtilities.CreateInstance<FileStorageProvider>(services, name, options.RootDirectory);
        }
    }
}

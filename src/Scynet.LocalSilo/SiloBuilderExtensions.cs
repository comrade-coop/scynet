using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Options;
using Orleans;
using Orleans.Hosting;
using Orleans.Runtime;
using Orleans.Storage;
using System;
using System.Collections.Generic;
using System.Text;

namespace Scynet.LocalSilo
{
    public static class SiloBuilderExtensions
    {
        public static ISiloHostBuilder AddFileGrainStorage(this ISiloHostBuilder builder, string providerName,
            Action<FileStorageOptions> options)
        {
            return builder.ConfigureServices(services => services.AddFileGrainStorage(providerName, ob => ob.Configure(options)));
        }

        public static IServiceCollection AddFileGrainStorage(this IServiceCollection services,
            string providerName, Action<OptionsBuilder<FileStorageOptions>> options)
        {
            options?.Invoke(services.AddOptions<FileStorageOptions>(providerName));
            return services
                .AddSingletonNamedService(providerName, FileGrainStorageFactory.Create)
                .AddSingletonNamedService(providerName,
                    (s, n) => (ILifecycleParticipant<ISiloLifecycle>)s.GetRequiredServiceByName<IGrainStorage>(n));
        }
    }
}

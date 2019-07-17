package configurations

import org.apache.ignite.configuration.CacheConfiguration
import org.apache.ignite.configuration.IgniteConfiguration

class IgniteConfigurationBuilder {
    private val config = IgniteConfiguration()

    private  val cacheConfigurations = mutableListOf<CacheConfiguration<*,*>>()


    fun cache(lambda: CacheConfigurationBuilder.() -> Unit){
        cacheConfigurations.add(CacheConfigurationBuilder().apply(lambda).build())
    }

    var name = ""

    fun build(): IgniteConfiguration {
        config.apply {
            setPeerClassLoadingEnabled(true)
            igniteInstanceName = name
            for (configuration in cacheConfigurations) {
                setCacheConfiguration(configuration)
            }
        }
        return config
    }
}



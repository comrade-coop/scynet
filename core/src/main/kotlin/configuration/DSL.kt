package ai.scynet.core.configuration

import org.apache.ignite.configuration.CacheConfiguration
import org.apache.ignite.configuration.IgniteConfiguration


class  Cache(val configuration: CacheConfiguration<*, *>)

class CACHES: ArrayList<Cache>(){
    fun cache(lambda: CacheBuilder.() -> Unit){
        add(CacheBuilder().apply(lambda).build())
    }
}

class CacheBuilder{
    var name: String = "hello"
    var backups: Int = 0

    fun build(): Cache = Cache(CacheConfiguration<Any, Any>()
            .setStoreKeepBinary(true)
            .setName(name)
            .setBackups(backups)
    )
}


class IgniteConfigurationBuilder {
    private val config = IgniteConfiguration()

    private  val caches = mutableListOf<Cache>()

    fun caches(lambda: CACHES.() -> Unit){
        caches.addAll(CACHES().apply(lambda))
    }

    fun build(): IgniteConfiguration {
        config.apply {
            setPeerClassLoadingEnabled(true)
            for (cache in caches) {
                setCacheConfiguration(cache.configuration)
            }
        }
        return config
    }
}


fun ignite(lambda: IgniteConfigurationBuilder.() -> Unit): IgniteConfiguration = IgniteConfigurationBuilder().apply(lambda).build()

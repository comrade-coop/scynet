package configurations

import org.apache.ignite.configuration.CacheConfiguration

class CacheConfigurationBuilder{
    var name: String = "hello"
    var backups: Int = 0

    fun build(): CacheConfiguration<*,*> = CacheConfiguration<Any, Any>()
            .setStoreKeepBinary(true)
            .setName(name)
            .setBackups(backups)
}

import ai.scynet.core.configurations.ConfigurationBase
import io.kotlintest.Spec
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import ai.scynet.core.configurations.Base
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.CacheConfiguration
import org.apache.ignite.configuration.IgniteConfiguration


class DSLIgniteConfigurationTest: StringSpec(){
    lateinit var ignite: Ignite
    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        val base = ConfigurationBase()
        val config = base.ignite {
            igniteInstanceName = "No Name"
            cache{
                name = "NoDSL"
            }
        }
        ignite = Ignition.start(config)
    }

    override fun afterSpec(spec: Spec) {
        super.afterSpec(spec)
        ignite.destroyCaches(ignite.cacheNames())
        ignite.close()
    }
    //Should be taken from ConfigurationBase but for some reason is not
    fun IgniteConfiguration.cache(lamda: CacheConfiguration<*, *>.() -> Unit){
        this.setCacheConfiguration(CacheConfiguration<Any,Any>().apply(lamda))
    }

    init {
        "Test cache works"{
            val cache = ignite.getOrCreateCache<String,String>("GTHANG")
            cache.put("hello", "world")
            val result = cache.get("hello")
            result shouldBe "world"
            ignite.destroyCache("GTHANG")
        }

        "Test NoDSL cache exists"{
            val DSLcacheExists = ignite.cacheNames().contains("NoDSL")
            DSLcacheExists shouldBe true
        }
    }
}

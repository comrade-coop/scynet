import configurations.ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import kotlin.test.*


class DSLIgniteConfigurationTest{
    companion object {
        val config: IgniteConfiguration = ignite {
            cache {
                name = "DSL"
                backups = 1
            }
        }

        val ignite = Ignition.start(config)
    }

    @Test fun testCacheWorks(){
        val cache = ignite.getOrCreateCache<String,String>("GTHANG")
        cache.put("hello", "world")
        val result = cache.get("hello")
        assertEquals("world", result, "Should return hello.")
        ignite.destroyCache("GTHANG")
    }

    @Test fun testCacheExists(){
        val DSLcacheExists = ignite.cacheNames().contains("DSL")
        assertTrue(DSLcacheExists, "should contain cache named DSL.")
    }
}

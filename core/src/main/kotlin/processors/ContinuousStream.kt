package processors

import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.query.ContinuousQuery
import org.apache.ignite.cache.query.ScanQuery
import org.apache.ignite.cluster.ClusterGroup
import org.apache.ignite.lang.IgniteRunnable
import org.apache.ignite.resources.IgniteInstanceResource
import org.apache.ignite.services.Service
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.lang.Exception
import java.util.*
import javax.cache.event.CacheEntryEvent
import javax.cache.event.CacheEntryUpdatedListener

open class ContinuousStream<V>(private val id: UUID, private val streamService: IContinuousStreamService ): KoinComponent {
    private val ignite: Ignite by inject()
    private lateinit var cache: IgniteCache<Long, V>
    val classId = "ContinuesStream"



    var serviceInstance: ContinuousStreamService<V>? = null

    init {
        cache = ignite.getOrCreateCache(getCacheName())
    }
    protected fun getCacheName():String {
        return "STREAM|CLASS:$classId|ID:$id"
    }

    protected open fun getDeploymentNodes(): ClusterGroup? {
        return null
    }

    protected fun getOrCreateService(): ContinuousStreamService<V> {
        if (serviceInstance == null) {
            try {
                getServiceProxy()
            } catch(e: Exception) {
                val nodes = getDeploymentNodes()
                val newService = streamService//ContinuousStreamService<V>(30)
                ignite.services().deployClusterSingleton(getCacheName(), newService)
                getServiceProxy()
            }
        }
        return serviceInstance!!
    }

    private fun getServiceProxy(){
        serviceInstance = ignite.services().serviceProxy(id.toString(), streamService::class.java,true)
    }

    protected open fun engageLiveStream() {
        var service = getOrCreateService()
        service.engageLiveStream()
        //TODO: Start timer to engage every service.engagementTimeoutSeconds/2
    }

    open fun fillMissingStreamData(from: Long, to: Long)  = getOrCreateService().fillMissingStreamData(from, to)

    open fun fillMissingStreamData(from: Long) = getOrCreateService().fillMissingStreamData(from)

    open fun refreshStreamData(from: Long, to: Long) = getOrCreateService().refreshStreamData(from, to)

    open fun refreshStreamData(from: Long) = getOrCreateService().refreshStreamData(from)

    //TODO: Allow more elaborate querying interface
     fun <K, V> listen(callback: (K, V, V?) -> Unit) : AutoCloseable {
        engageLiveStream()
        var query = ContinuousQuery<K, V>()

        query.setLocalListener{ evts ->
            evts.forEach{ e -> callback(e.key, e.value, e.oldValue)}
        }
        query.initialQuery = ScanQuery<K,V>()
        var cursor = cache.query(query)
        return cursor
     }
}
package processors

import descriptors.LazyStreamDescriptor
import descriptors.LazyStreamServiceDescriptor
import descriptors.Properties
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheEntryEventSerializableFilter
import org.apache.ignite.cache.query.ContinuousQuery
import org.apache.ignite.cache.query.QueryCursor
import org.apache.ignite.cache.query.ScanQuery
import org.apache.ignite.cluster.ClusterGroup
import org.apache.ignite.services.ServiceDeploymentException
import org.apache.ignite.services.ServiceDescriptor
import org.apache.logging.log4j.LogManager
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import javax.cache.Cache
import kotlin.collections.ArrayList
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

abstract class LazyStream<K, V>(): ILazyStream, KoinComponent {
    protected val logger = LogManager.getLogger(this::class.qualifiedName)!!
    override var descriptor: LazyStreamDescriptor? = null
        set(value) {
            field = value
            cache = ignite.getOrCreateCache(descriptor!!.id.toString())
        }
    private val ignite: Ignite by inject()
    private var cache: IgniteCache<Long, V>? = null
    private var serviceInstance: ILazyStreamService? = null
    private val  serviceEngagementTimer: ServiceEngagementTimer = ServiceEngagementTimer()
    protected abstract val streamServiceClass: KClass<out ILazyStreamService>

    constructor(id: UUID, inputStreamIds: ArrayList<UUID>?, serviceProperties: Properties?): this(){
        descriptor = LazyStreamDescriptor(
                id = id,
                serviceDescriptor = LazyStreamServiceDescriptor(
                        inputStreamIds = inputStreamIds,
                        properties = serviceProperties
                )
        )
    }

    private inner class ServiceEngagementTimer(private val delay: Long = 10000){
        private val  timer = Timer(true)
        private  var task: TimerTask = getTimerTask()
        fun start(){
            task =  getTimerTask()
            timer.scheduleAtFixedRate(task, delay,delay)
        }

        fun stop(){
            task.cancel()
            timer.purge()
        }
        private fun getTimerTask(): TimerTask = object : TimerTask(){
            override fun run() {
                engageLiveStream()
            }
        }
    }

    private fun descriptorContainsServiceName(descriptor: ServiceDescriptor): Boolean{
        if(descriptor.name() == this.descriptor!!.id.toString())
            return true
        return false
    }

    private fun getServiceProxy(): ILazyStreamService{
        return ignite.services().serviceProxy(descriptor!!.id.toString(), ILazyStreamService::class.java,true)
    }

    private fun engageLiveStream(){
        serviceInstance?.engageLiveStream()
    }

    private  fun engageLiveStreamTimer() {
        if(serviceInstance == null){
            getOrCreateService()
        }
        serviceEngagementTimer.start()
    }
    private fun getDeploymentNodes(): ClusterGroup? {
        return null
    }

    protected fun getOrCreateService(): ILazyStreamService {
        if(serviceInstance == null) {
            val serviceDescriptors = ignite.services().serviceDescriptors().filter(::descriptorContainsServiceName)
            if (serviceDescriptors.isEmpty()) {
                val nodes = getDeploymentNodes()
                val streamService = streamServiceClass.createInstance()
                streamService.descriptor = descriptor!!.serviceDescriptor
                try{
                    ignite.services().deployClusterSingleton(descriptor!!.id.toString(), streamService)
                }catch (e: ServiceDeploymentException) {
                    logger.error(e)
                }
            }
            serviceInstance = getServiceProxy()
        }
        return serviceInstance!!
    }

    override fun fillMissingStreamData(from: Long, to: Long)  = serviceInstance!!.fillMissingStreamData(from, to)

    override fun fillMissingStreamData(from: Long) = serviceInstance!!.fillMissingStreamData(from)

    override fun refreshStreamData(from: Long, to: Long) = serviceInstance!!.refreshStreamData(from, to)

    override fun refreshStreamData(from: Long) = serviceInstance!!.refreshStreamData(from)

    override fun <K, V> listen(callback: (K, V, V?) -> Unit) : QueryCursor<Cache.Entry<K,V>> {
        engageLiveStreamTimer()

        val query = ContinuousQuery<K, V>()

        query.setLocalListener{ evts ->
            evts.forEach{ e -> callback(e.key, e.value, e.oldValue)}
        }
        query.initialQuery = ScanQuery<K,V>()

        return cache!!.query(query)
     }

    override fun <K, V> listen(predicate: (K, V) -> Boolean, callback: (K, V, V?) -> Unit): AutoCloseable {
        engageLiveStreamTimer()

        val query = ContinuousQuery<K, V>()

        query.setLocalListener{ evts ->
            evts.forEach{ e -> callback(e.key, e.value, e.oldValue)}
        }
        query.remoteFilter = CacheEntryEventSerializableFilter {
            predicate(it.key, it.value)
        }
        query.initialQuery = ScanQuery<K,V>()

        return cache!!.query(query)
    }

    override fun dispose(){
        serviceEngagementTimer.stop()
        serviceInstance = null
        logger.info("Engagement timer for ${descriptor!!.id} successfully stopped!")
    }

    override fun getCachee(): IgniteCache<Any, Any> {
        return cache as IgniteCache<Any, Any>
    }
}
package processors

import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.query.ContinuousQuery
import org.apache.ignite.cache.query.ScanQuery
import org.apache.ignite.cluster.ClusterGroup
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

abstract class LazyStream<V>(private val id: UUID, private val streamService: ILazyStreamService<V> ): KoinComponent {
    private val ignite: Ignite by inject()
    private val cache: IgniteCache<Long, V>
    private val classId = this::class.java.name
    private lateinit var serviceInstance: ILazyStreamService<V>
    private lateinit var  serviceEngagementTimer: ServiceEngagementTimer

    private inner class ServiceEngagementTimer(private val delay: Long){
        private val  timer = Timer(true)
        private lateinit var task: TimerTask
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

    init {
        cache = ignite.getOrCreateCache(getCacheName())
    }
    private fun getCacheName():String {
        return "STREAM|CLASS:$classId|ID:$id"
    }

    private fun getOrCreateService(): ILazyStreamService<V> {
        if (!::serviceInstance.isInitialized) {
            try {
                getServiceProxy()
                serviceInstance.engagementTimeoutSeconds
            } catch(e: org.apache.ignite.IgniteException) {
                val nodes = getDeploymentNodes()
                ignite.services().deployClusterSingleton(getCacheName(), streamService)
                getServiceProxy()
            }
        }
        return serviceInstance
    }

    private fun getServiceProxy(){
        serviceInstance = ignite.services().serviceProxy(getCacheName(), ILazyStreamService::class.java,true)
    }

    private fun engageLiveStream(){
        serviceInstance.engageLiveStream()
    }

    private  fun engageLiveStreamTimer() {
        if(!::serviceInstance.isInitialized){
            getOrCreateService()
        }
        if(!::serviceEngagementTimer.isInitialized){
            serviceEngagementTimer = ServiceEngagementTimer(serviceInstance.engagementTimeoutSeconds * 500L)
        }
        serviceEngagementTimer.start()
    }
    private fun getDeploymentNodes(): ClusterGroup? {
        return null
    }
    open fun fillMissingStreamData(from: Long, to: Long)  = serviceInstance.fillMissingStreamData(from, to)

    open fun fillMissingStreamData(from: Long) = serviceInstance.fillMissingStreamData(from)

    open fun refreshStreamData(from: Long, to: Long) = serviceInstance.refreshStreamData(from, to)

    open fun refreshStreamData(from: Long) = serviceInstance.refreshStreamData(from)

    //TODO: Allow more elaborate querying interface
     fun <K, V> listen(callback: (K, V, V?) -> Unit) : AutoCloseable {
        engageLiveStreamTimer()

        val query = ContinuousQuery<K, V>()

        query.setLocalListener{ evts ->
            evts.forEach{ e -> callback(e.key, e.value, e.oldValue)}
        }
        query.initialQuery = ScanQuery<K,V>()

        return cache.query(query)
     }

    open fun disengageStream(){
        serviceEngagementTimer.stop()
        println("${getCacheName()} disengaged successfully!")
    }
}
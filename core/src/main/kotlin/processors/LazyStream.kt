package processors

import descriptors.LazyStreamDescriptor
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.query.ContinuousQuery
import org.apache.ignite.cache.query.ScanQuery
import org.apache.ignite.cluster.ClusterGroup
import org.apache.ignite.services.ServiceDescriptor
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.reflect.full.createInstance

abstract class LazyStream<V>: ILazyStream, KoinComponent {
    override var descriptor: LazyStreamDescriptor? = null
    private val ignite: Ignite by inject()
    private var cache: IgniteCache<Long, V>? = null
    private var serviceInstance: ILazyStreamService? = null
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



    private fun descriptorContainsServiceName(descriptor: ServiceDescriptor): Boolean{
        if(descriptor.name() == this.descriptor!!.id.toString())
            return true
        return false
    }

    private fun getServiceProxy(): ILazyStreamService{
        return ignite.services().serviceProxy(descriptor!!.id.toString(), ILazyStreamService::class.java,true)
    }

    private fun engageLiveStream(){
        serviceInstance!!.engageLiveStream()
    }

    private  fun engageLiveStreamTimer() {
        if(serviceInstance == null){
            getOrCreateService()
        }
        if(!::serviceEngagementTimer.isInitialized){
            serviceEngagementTimer = ServiceEngagementTimer(serviceInstance!!.engagementTimeoutSeconds * 500L)
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
                val streamService = descriptor!!.serviceDescriptor.streamServiceClass.createInstance()
                streamService.descriptor = descriptor!!.serviceDescriptor
                ignite.services().deployClusterSingleton(descriptor!!.id.toString(), streamService)
            }
            serviceInstance = getServiceProxy()
        }
        return serviceInstance!!
    }

    override fun fillMissingStreamData(from: Long, to: Long)  = serviceInstance!!.fillMissingStreamData(from, to)

    override fun fillMissingStreamData(from: Long) = serviceInstance!!.fillMissingStreamData(from)

    override fun refreshStreamData(from: Long, to: Long) = serviceInstance!!.refreshStreamData(from, to)

    override fun refreshStreamData(from: Long) = serviceInstance!!.refreshStreamData(from)

    //TODO: Allow more elaborate querying interface
    override fun <K, V> listen(callback: (K, V, V?) -> Unit) : AutoCloseable {
        //TODO: remove cache initialization from here
        cache = ignite.getOrCreateCache(descriptor!!.id.toString())
        engageLiveStreamTimer()

        val query = ContinuousQuery<K, V>()

        query.setLocalListener{ evts ->
            evts.forEach{ e -> callback(e.key, e.value, e.oldValue)}
        }
        query.initialQuery = ScanQuery<K,V>()

        return cache!!.query(query)
     }

    override fun dispose(){
        serviceEngagementTimer.stop()
        serviceInstance = null
        println("${descriptor!!.id} disposed successfully!")
    }
}
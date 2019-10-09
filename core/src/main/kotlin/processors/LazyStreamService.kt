package processors

import descriptors.LazyStreamDescriptor
import descriptors.LazyStreamServiceDescriptor
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.services.ServiceContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.lang.IllegalStateException
import java.lang.NullPointerException
import java.util.*
import kotlin.collections.ArrayList

abstract class LazyStreamService<K, V> : ILazyStreamService, KoinComponent {
    protected val logger = LogManager.getLogger(this::class.qualifiedName)!!

    override var descriptor: LazyStreamServiceDescriptor? = null
    protected  val ignite: Ignite by inject()
    protected lateinit var context: ServiceContext
    // cache always has UNIX timestamp as key
    protected lateinit var cache: IgniteCache<K,V>
    protected lateinit var serviceName: String
    protected lateinit var serviceClassAndName: String
    private lateinit var countDown: CountDown
    private lateinit var inputStreamFactory: ILazyStreamFactory
    protected lateinit var inputStreams: ArrayList<ILazyStream>

    private inner class CountDown{
        private val timer = Timer(true)
        private  var task: TimerTask = getTimerTask()
        fun start(){
            try {
                task = getTimerTask()
                logger.trace("Starting CountDown for $serviceClassAndName!")
                this.timer.schedule(task, 20000)
            }catch (e: IllegalStateException){
                logger.error(e)
                logger.error(e.stackTrace)
            }

        }

        fun restart(){
            stop()
            start()
        }

        private fun stop(){
            try{
                task.cancel()
                timer.purge()
            }catch (e: IllegalStateException){
                logger.error(e)
                logger.error(e.stackTrace)
            }

        }

        private fun getTimerTask(): TimerTask = object : TimerTask(){
            override fun run() {
                ignite.services().cancel(serviceName)
            }
        }
    }

    override fun init(ctx: ServiceContext?) {
        context = ctx!!
        serviceName = context.name()
        serviceClassAndName = "${this::class.simpleName} - $serviceName"
        cache = ignite.getOrCreateCache(serviceName)
        if(descriptor!!.inputStreamIds != null){
            inputStreamFactory = ignite.services().serviceProxy("lazyStreamFactory", ILazyStreamFactory::class.java, false)
            inputStreams = ArrayList()  
            for(stream in descriptor!!.inputStreamIds!! ){
                inputStreams.add(inputStreamFactory.getInstance(stream)!!)
            }
        }
        if(!::countDown.isInitialized){
            countDown = CountDown()
        }
        countDown.start()
        logger.info("Service for $serviceClassAndName is initialized successfully!")
    }

    override fun execute(ctx: ServiceContext?) {
        logger.info("Starting service for $serviceClassAndName")
    }
    override fun cancel(ctx: ServiceContext?) {
        if(descriptor!!.inputStreamIds != null){
            for(stream in inputStreams){
                stream.dispose()
            }
        }
        logger.info("Service for $serviceClassAndName successfully cancelled!")
    }

    override fun engageLiveStream() {
        try{
            this.countDown.restart()
        }catch (e: UninitializedPropertyAccessException){
            logger.error(e)
            logger.error(e.stackTrace)
        }
    }

    override fun fillMissingStreamData(from: Long, to: Long) {

    }

    override fun fillMissingStreamData(from: Long) {

    }

    override fun refreshStreamData(from: Long, to: Long) {

    }

    override fun refreshStreamData(from: Long) {

    }
}
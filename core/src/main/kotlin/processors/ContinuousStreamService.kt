package processors

import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.resources.IgniteInstanceResource
import org.apache.ignite.services.Service
import org.apache.ignite.services.ServiceContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

abstract class ContinuousStreamService<V>(override val engagementTimeoutSeconds: Int) : IContinuousStreamService, KoinComponent {

    private  val ignite: Ignite by inject()
    private lateinit var context: ServiceContext
    // cache always has UNIX timestamp as key
    protected lateinit var cache: IgniteCache<Long,V>
    protected lateinit var serviceName: String
    private lateinit var countDown: CountDown

    override fun init(ctx: ServiceContext?) {
        context = ctx!!
        serviceName = context.name()
        cache = ignite.getOrCreateCache(serviceName)
        println("$serviceName is initialized successfully!")
        countDown = CountDown()
        countDown.start()
    }

    override fun cancel(ctx: ServiceContext?) {
        cache.close()
        println("$serviceName successfully cancelled!")
    }
    override fun engageLiveStream() {
        countDown.restart()
    }



    override fun fillMissingStreamData(from: Long, to: Long) {

    }

    override fun fillMissingStreamData(from: Long) {

    }

    override fun refreshStreamData(from: Long, to: Long) {

    }

    override fun refreshStreamData(from: Long) {

    }

    internal inner class CountDown{
        private lateinit var timer: Timer
        private lateinit var task: TimerTask
        fun start(){
            println("Starting CountDown!")
            timer = Timer(false)
            task =  object : TimerTask(){
                override fun run() {
                    cancel(context)
                }
            }
            timer.schedule(task, engagementTimeoutSeconds.toLong() * 1000)
        }
        fun restart(){
            timer.cancel()
            start()
        }
        fun stop(){
            timer.cancel()
        }
    }
}
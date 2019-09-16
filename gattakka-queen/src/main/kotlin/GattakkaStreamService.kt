package ai.scynet.queen

import ai.scynet.protocol.TrainingJob
import ai.scynet.queen.CustomReceiverIndividualActor
import ai.scynet.queen.GattakaQueenHelper
import akka.actor.ActorSystem
import com.obecto.gattakka.Evaluator
import com.obecto.gattakka.Pipeline
import com.obecto.gattakka.Population
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.query.ContinuousQuery
import org.apache.ignite.cache.query.ScanQuery
import org.apache.ignite.services.ServiceContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import processors.LazyStreamService
import java.lang.IllegalStateException
import java.time.Instant
import java.util.*

class GattakkaStreamService: LazyStreamService<Long, TrainingJob>() {

    lateinit var tempJobCache: IgniteCache<Long, TrainingJob>

    override fun init(ctx: ServiceContext?) {
        super.init(ctx)

        tempJobCache = ignite.getOrCreateCache<Long, TrainingJob>("tmp_jobs")
    }

    override fun execute(ctx: ServiceContext?) {
        val system = ActorSystem.apply("gattakka")
        val helper = GattakaQueenHelper()
        super.execute(ctx)

        val pipelineActor = system.actorOf(Pipeline.props(helper.pipelineOperators()), "pipeline")
        val evaluator = system.actorOf(Evaluator.props(helper.evaluator()), "evaluator")
        val populationActor = system.actorOf(Population.props(
                CustomReceiverIndividualActor::class.java,
                helper.initialChromosomes(),
                evaluator,
                pipelineActor,
                null
        ), "population")

        val query = ContinuousQuery<Long, TrainingJob>()

        query.setLocalListener{ evts ->
            evts.forEach{ e -> this@GattakkaStreamService.cache.put(e.key, e.value) }
        }

        query.initialQuery = ScanQuery<Long,TrainingJob>()

        tempJobCache!!.query(query)


        helper.refreshPopulation(populationActor)

        system.systemImpl().start()
        println("Started")

    }

    override fun cancel(ctx: ServiceContext?) {

        super.cancel(ctx)
    }

}
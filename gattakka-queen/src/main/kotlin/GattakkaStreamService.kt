package ai.scynet.queen

import ai.scynet.protocol.TrainingJob
import ai.scynet.queen.CustomReceiverIndividualActor
import ai.scynet.queen.GattakaQueenHelper
import akka.actor.ActorSystem
import com.obecto.gattakka.Evaluator
import com.obecto.gattakka.Pipeline
import com.obecto.gattakka.Population
import org.apache.ignite.services.ServiceContext
import processors.LazyStreamService
import java.lang.IllegalStateException
import java.time.Instant
import java.util.*

class GattakkaStreamService: LazyStreamService<TrainingJob>() {


    override fun init(ctx: ServiceContext?) {
        super.init(ctx)

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

        helper.refreshPopulation(populationActor)

        system.systemImpl().start()
        println("Started")

    }

    override fun cancel(ctx: ServiceContext?) {

        super.cancel(ctx)
    }

}
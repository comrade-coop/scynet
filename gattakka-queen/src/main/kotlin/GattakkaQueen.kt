package ai.scynet.queen

import ai.scynet.common.LifeCycle
import akka.actor.ActorSystem
import com.obecto.gattakka.*
import org.koin.core.KoinComponent

class GattakkaQueen : LifeCycle, KoinComponent {
    val system = ActorSystem.apply("gattakka")
    val helper = GattakaQueenHelper()


    override fun start() {
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

    override fun stop() {

    }
}

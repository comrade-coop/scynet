package ai.scynet.queen

import com.obecto.gattakka.messages.population.{IntroducePopulation, PipelineFinishedEvent, RefreshPopulation}
import com.obecto.gattakka.messages.evaluator._
import com.obecto.gattakka.messages.eventbus.AddSubscriber
import com.obecto.gattakka.{Evaluator}
import scala.concurrent.duration._

class QueenEvaluator extends Evaluator {

  import com.obecto.gattakka.messages.evaluator._
  import context.dispatcher
/*
  var requiredAmount = 10
  var refreshed = false
  var scheduled = false
  val requiredRatio = 0.0

  def tryRefresh() = {
    if (!scheduled) {
      scheduled = true
      context.system.scheduler.scheduleOnce(30.seconds) {
        scheduled = false
        if (fitnesses.size >= requiredAmount && !refreshed) {
          refreshed = true
          populationActor ! RefreshPopulation(false)
        }
      }
    }
  }

  override def customReceive = {
    case IntroducePopulation =>
      originalReceive(IntroducePopulation)
      populationActor ! AddSubscriber(self, classOf[PipelineFinishedEvent])

    case PipelineFinishedEvent(totalSize, newComers) =>
      requiredAmount = totalSize - newComers + (newComers * requiredRatio).ceil.toInt
      refreshed = false
      tryRefresh()

    case x @ (_: SetFitness) =>
      originalReceive(x)
      tryRefresh()
  }
 */

  context.system.scheduler.schedule(60.seconds, 60.seconds) {
    println("Refreshing population")
    populationActor ! RefreshPopulation(false)
  }
}

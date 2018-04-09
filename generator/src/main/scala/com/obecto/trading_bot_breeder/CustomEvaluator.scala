package com.obecto.trading_bot_breeder

// import akka.actor.{ActorRef}
import com.obecto.gattakka.messages.population.{IntroducePopulation, PipelineFinishedEvent, RefreshPopulation}
import com.obecto.gattakka.messages.evaluator._
import com.obecto.gattakka.messages.eventbus.AddSubscriber
import com.obecto.gattakka.{Evaluator}
import scala.concurrent.duration._

class CustomEvaluator extends Evaluator {

  import com.obecto.gattakka.messages.evaluator._
  import context.dispatcher

  var requiredAmount = 10
  var refreshed = false
  var scheduled = false
  val requiredRatio = 1.0

  def tryRefresh() = {
    if (!scheduled) {
      scheduled = true
      context.system.scheduler.scheduleOnce(1.seconds) {
        scheduled = false
        if (fitnesses.size >= requiredAmount && requiredAmount > 0 && !refreshed) {
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
}

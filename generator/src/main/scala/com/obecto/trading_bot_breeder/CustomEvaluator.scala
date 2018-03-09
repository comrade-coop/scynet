package com.obecto.trading_bot_breeder

// import akka.actor.{ActorRef}
import com.obecto.gattakka.messages.population.RefreshPopulation
import com.obecto.gattakka.{EvaluationAgent, Evaluator}
import scala.concurrent.duration._
import scala.language.postfixOps

class CustomEvaluator(evaluationAgentType: Class[_ <: EvaluationAgent], environmentalData: Any)
  extends Evaluator(evaluationAgentType, environmentalData){
  import context.dispatcher
  context.system.scheduler.schedule(1 minute, 1 minute) {
    println("refr")
    populationActor ! RefreshPopulation(false)
  }

}

package Actors

import Actors.EggRegistry.{EggData, GetEgg}
import Actors.HatcheryController._
import Actors.HyrdaliskProxy.{AllowConnection, DenyConnection}
import Actors.ScynetConnector.Auth
import akka.actor.Actor
import akka.util.Timeout

import scala.concurrent.{Await, Promise}
import scala.util.{Failure, Success}

object HatcheryController {
  case class Run()
  case class RunEgg(egg: String, performance: Double)
  case class NewConnection()
  case class Connection()
}
class HatcheryController() extends Actor {
  import context.dispatcher

  val scynet = context.actorSelection("/user/scynet")
  val hydralisk = context.actorSelection("/user/hydralisk")
  val queen = context.actorSelection("/user/queen")
  val registry = context.actorSelection("/user/registry")
  val cluster = context.actorSelection("/user/cluster")
  // TODO: [External] 1 st, make a producer that publishes eggs on kafka.
  // TODO: [External] 2 nd, make it possible run eggs without consuming any data at the moment, just to have a protocol of agent execution

  override def receive = {
    case Run => {

    }
    case EggProduced(egg: String, performance: Double) => {

    }
    case RunEgg(egg, performance) => {
      val result = Promise[EggData]
      registry ! GetEgg("", result)
      for(egg <- result.future){
        println(s"Execute: $egg")
      }
    }
    case NewConnection() => {
      println("new connection")
      val result = Promise[Boolean]
      scynet ! Auth("obecto", result) // TODO: Should come from connection
      result.future.onComplete {
        case Success(value) => {
          if(value) {
            hydralisk ! AllowConnection()
          }else{
            hydralisk ! DenyConnection("permission")
          }
        }
        case Failure(ex) => {
          hydralisk ! DenyConnection("error")
        }
      }
    }
  }
}

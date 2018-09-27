package Actors

import Actors.EggRegistry.{CommitEgg, Egg, EggData, GetEgg}
import akka.actor.Actor

import scala.collection.mutable
import scala.concurrent.Promise


object EggRegistry {
  case class CommitEgg(egg: String, performance: Double)
  case class GetEgg(egg: String, result: Promise[EggData])
  case class EggData(name: String, performance: Double)
}
class EggRegistry extends Actor {
  var eggs: mutable.HashMap[String, EggData] = mutable.HashMap()

  override def receive = {
    case GetEgg(egg, result) => {
      result success eggs(egg)
    }
    case CommitEgg(egg, performance) => {
      eggs.put(egg, EggData(egg, performance))
    }
  }
}

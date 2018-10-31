package Actors

import Actors.EggRegistry._
import akka.actor.Actor

import scala.collection.mutable
import scala.concurrent.Promise

// TODO: Implement publish strategies, shouldn't we just publish all the eggs ???

object EggRegistry {
  case class CommitEgg(egg: String, performance: Double)
  case class GetEgg(egg: String)
  case class EggData(name: String, performance: Double)
}
class EggRegistry extends Actor {
  var eggs: mutable.HashMap[String, EggData] = mutable.HashMap()

  override def receive = {
    case GetEgg(egg) => {
      sender() ! eggs(egg)
    }
    case CommitEgg(egg, performance) => {
      eggs.put(egg, EggData(egg, performance))
    }
  }
}

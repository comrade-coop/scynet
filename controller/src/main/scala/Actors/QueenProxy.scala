package Actors

import Actors.QueenProxy.EggProduced
import akka.actor.{Actor, Props}
import main.scala.Actors.Util.KafkaReceiver.{Record, SubscribeTopic}

object QueenProxy {
  def props(name: String) = Props(new QueenProxy(name))
  case class EggProduced(egg: String, performance: Double)
}
class QueenProxy(name: String) extends Actor {
  val receiver = context.actorSelection("/user/receiver")
  val controller = context.actorSelection("/user/controller")

  lazy val topic = s"eggs-$name"

  override def preStart(): Unit = {
    super.preStart()
    receiver ! SubscribeTopic(topic, None)

  }
  override def receive = {
    case "start_making_eggs" => {
      // TODO: comunicate with queen to make agents.
    }
    case Record(`topic`, _, value: String) => {
      controller ! EggProduced(value, 0.5)
    }
  }
}

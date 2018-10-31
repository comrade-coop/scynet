package Actors

import Actors.HyrdaliskProxy._
import Actors.ScynetConnector.{Auth}
import akka.actor.{Actor, ActorRef}

object HyrdaliskProxy {
  case class NewConnection()
  case class AllowConnection()
  case class DenyConnection(reason: String)
}
class HyrdaliskProxy() extends Actor {
  override def receive = {

    case AllowConnection() => {
      println("Allowed connection")
    }
    case DenyConnection(reason: String) =>
    {
      println(s"Denied connection: ${reason}")
    }
  }
}

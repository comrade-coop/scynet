package Actors

import Actors.ScynetConnector.Auth
import akka.actor.Actor
import scala.concurrent.Promise

object ScynetConnector {
  case class Auth(token: String)
}
class ScynetConnector extends Actor {
  override def receive = {
    case Auth(token) => {
      if(token == "obecto"){
        sender() ! true
      }else {
        sender() ! false

      }
    }
  }
}

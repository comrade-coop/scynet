package Actors

import Actors.ScynetConnector._
import akka.actor.Actor

import scala.concurrent.Promise

object ScynetConnector {
  case class Auth(token: String, result: Promise[Boolean])
}
class ScynetConnector extends Actor {
  override def receive = {
    case Auth(token, promise) => {
      if(token == "obecto"){
        promise.success(true)
      }else {
        promise.success(false)
      }
    }
  }
}

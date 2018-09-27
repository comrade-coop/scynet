package Actors

import akka.actor.Actor

object QueenProxy {

}
class QueenProxy extends Actor {
  override def receive = {
    case "start_making_eggs" => {
      // TODO: comunicate with queen to make agents.
    }
  }
}

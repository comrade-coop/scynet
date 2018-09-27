package Actors

import java.net.URL

import Actors.ClusterController._
import akka.actor.{Actor, Props}


object ClusterController {
  case class StartQueenName(name: String)
  case class StartQueenURL(url: URL)
  case class StartAgent(egg: String)
}
class ClusterController extends Actor {
  override def receive = {
    case StartQueenName(name) => {
      context.actorOf(Props[QueenProxy], s"queen_$name")
    }
    case StartQueenURL(url) => {

    }
    case StartAgent(name) => {

    }
  }
}

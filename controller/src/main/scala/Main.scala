package ai.scynet


import Actors.HatcheryController.NewConnection
import Actors.{ClusterController, HatcheryController, HyrdaliskProxy, ScynetConnector}
import akka.actor.{ActorSystem, Props}
import akka.dispatch.ExecutionContexts

import scala.concurrent.Promise
import scala.concurrent.ExecutionContext.Implicits.global

object Main {
  def main(args: Array[String]) = {
    val system = ActorSystem("HatcheryController")

    system.actorOf(Props[ScynetConnector], "scynet")
    system.actorOf(Props[HyrdaliskProxy], "hydralisk")
    system.actorOf(Props[ClusterController], "cluster")

    val controller = system.actorOf(Props[HatcheryController], "hatcheryController")
//    val promise = Promise[Boolean]()


    controller ! NewConnection()

//    controller ! NewConnection()



  }
}

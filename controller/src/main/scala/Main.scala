package ai.scynet


import Actors.HatcheryController.NewConnection
import Actors._
import akka.actor.{ActorSystem, Props}
import cakesolutions.kafka.akka.KafkaConsumerActor
import com.typesafe.config.ConfigFactory
import main.scala.Actors.Util.KafkaReceiver
import org.apache.kafka.common.serialization._

object Main {
  def main(args: Array[String]) = {
    val config = ConfigFactory.load()
    val system = ActorSystem("HatcheryController")

    system.actorOf(Props[ScynetConnector], "scynet")
    system.actorOf(Props[HyrdaliskProxy], "hydralisk")
    system.actorOf(Props[QueenProxy], "queen")
    system.actorOf(Props[ClusterController], "cluster")
    system.actorOf(Props[EggRegistry], "registry")
    system.actorOf(KafkaReceiver.props(config), "receiver")

    val controller = system.actorOf(Props[HatcheryController], "hatcheryController")

    controller ! NewConnection()





  }
}

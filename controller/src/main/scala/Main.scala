package ai.scynet


import Actors.EggRegistry.{CommitEgg, GetEgg}
import Actors.HatcheryController.NewConnection
import Actors.QueenProxy.Start
import Actors._
import akka.actor.{ActorSystem, Props}
import cakesolutions.kafka.akka.KafkaConsumerActor
import com.typesafe.config.ConfigFactory
import main.scala.Actors.Util.KafkaReceiver
import org.apache.kafka.common.serialization._

object Main {
  def main(args: Array[String]) = {
//    val builder = new ProcessBuilder("/bin/nc", "-l",  "-p", "8080")
//
//    val thread = new Thread() {
//      override def run(): Unit = {
//        val process = builder.start()
//        for (elem <- scala.io.Source.fromInputStream(process.getInputStream).getLines()) {
//          println(elem)
//          if(elem == "close"){
//            println("bye")
//            process.destroy()
//          }
//        }
//      }
//    }
//
//
//    thread.run()
//
//
//
//    println("started?")
//
//    thread.join()
//    System.exit(0)

    val config = ConfigFactory.load()
    val system = ActorSystem("HatcheryController")

    system.actorOf(Props[ScynetConnector], "scynet")
    system.actorOf(Props[HyrdaliskProxy], "hydralisk")
    var queen = system.actorOf(QueenProxy.props("autokeras"), "queen")
    system.actorOf(Props[ClusterController], "cluster")
    system.actorOf(Props[EggRegistry], "registry")
    system.actorOf(KafkaReceiver.props(config), "receiver")

    val controller = system.actorOf(Props[HatcheryController], "hatcheryController")

    queen ! Start("queen")

    controller ! NewConnection()


//    system.actorSelection("akka://AutokerasQueen@127.0.0.1:25520/user/hello") ! CommitEgg("Hello", 12.2)

  }
}

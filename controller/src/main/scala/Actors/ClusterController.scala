package Actors

import java.net.URL
import Actors.ClusterController._
import akka.actor.{Actor, Props}
import akka.stream.{ActorMaterializer}
import play.api.libs.json.Json
import skuber.k8sInit
import skuber.apps.v1.Deployment
import skuber.json.format._
import scala.util.{Success, Failure}

object ClusterController {
  case class StartQueenName(name: String)
  case class StartQueenURL(url: URL)
  case class StartAgent(egg: String)
}
class ClusterController extends Actor {
  val materializer = ActorMaterializer()
  import context.dispatcher

  override def receive = {
    case StartQueenName(name) => {
      context.actorOf(Props[QueenProxy], s"queen_$name")
    }
    case StartQueenURL(url) => {

    }
    case StartAgent(egg) => {
      val k8s = k8sInit(context.system, materializer)


      val id = "123"

      val deployment = Json.obj( // Todo: get from queen / read from disk
        "apiVersion" -> "apps/v1",
        "kind" -> "Deployment",
        "spec" -> Json.obj(
          "selector" -> Json.obj(
            "matchLabels" -> Json.obj(
              "agent" -> "hello-world-agent"
            )
          ),
          "template" -> Json.obj(
            "spec" -> Json.obj(
              "containers" -> Json.arr(Json.obj(
                "image" -> "hello-world:linux",
                "env" -> Json.arr(Json.obj("name" -> "SCYNET_EGG", "value" -> egg)),
                "name" -> "hello-world"
              ))
            ),
            "metadata" -> Json.obj(
              "labels" -> Json.obj(
                "agent" -> "hello-world-agent"
              )
            )
          ),
          "replicas" -> 1
        ),
        "metadata" -> Json.obj(
          "name" -> s"hello-world-agent-$id"
        )
      ).as[Deployment]

      (k8s create deployment) onComplete {
        case Success(deployment) =>
          println(s"Sucessfully created ${deployment.metadata.name}")
          context.sender ! Some(deployment.metadata.name)
        case Failure(error) =>
          context.sender ! None
      }
    }
  }
}

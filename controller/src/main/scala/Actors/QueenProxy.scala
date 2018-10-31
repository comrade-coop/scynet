package Actors

import Actors.QueenProxy.{EggProduced, Start, Stop}
import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import main.scala.Actors.Util.KafkaReceiver.{Record, SubscribeTopic}
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.unmarshalling.Unmarshal
import spray.json.DefaultJsonProtocol._
import spray.json._

object QueenProxy {
  def props(name: String) = Props(new QueenProxy(name))
  case class EggProduced(egg: String, performance: Double)
  case class Start(name: String)
  case class Stop()
}
class QueenProxy(name: String) extends Actor {
  val receiver = context.actorSelection("/user/receiver")
  val controller = context.actorSelection("/user/controller")

  import akka.pattern.pipe
  import context.dispatcher

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  val http = Http(context.system)

  lazy val topic = s"eggs-$name"

  override def preStart(): Unit = {
    super.preStart()
    receiver ! SubscribeTopic(topic, None)

  }

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport =
    EntityStreamingSupport.json()

  override def receive = {
    case "start_making_eggs" => {
      // TODO: comunicate with queen to make agents.
    }
    case Record(`topic`, _, value: String) => {
      controller ! EggProduced(value, 0.5)
    }
    case Start(name) => {
      println(s"Starting: ${name}")
      for(responce <- http.singleRequest(HttpRequest(uri = s"http://127.0.0.1:8080/start/${name}"))){
        for(result <- Unmarshal(responce.entity).to[String]){
          println(s"Started ${name}")
//          println( result.parseJson.convertTo[List[String]](listFormat) )
        }
      }
    }
    case Stop => {

    }
  }
}

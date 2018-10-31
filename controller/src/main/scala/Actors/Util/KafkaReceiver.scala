package main.scala.Actors.Util

import akka.actor.{Actor, ActorRef, Props}
import cakesolutions.kafka.akka.KafkaConsumerActor._
import cakesolutions.kafka.akka.{ConsumerRecords, KafkaConsumerActor}
import com.typesafe.config.Config
import main.scala.Actors.Util.KafkaReceiver.{Record, SubscribeTopic}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
//
import scala.collection.mutable


// TODO: make better subscription so we can get multiple actors to receive the same topic
object KafkaReceiver {
  case class SubscribeTopic(name: String, deserializer: Any)
  case class Record[K, V](name: String, key: Option[K], value: V)

  def props(config: Config) = Props(new KafkaReceiver(config))
}
class KafkaReceiver(config: Config) extends Actor {


  val consumer = context.actorOf(
    KafkaConsumerActor.props(config, new StringDeserializer(), new StringDeserializer(), self),
    "KafkaConsumer"
  )

  val extractor = ConsumerRecords.extractor[String, String]
  var table = mutable.HashMap[String, ActorRef]()


  override def preStart() = {
    super.preStart()
    consumer ! Subscribe
  }

  override def postStop() = {
    consumer ! Unsubscribe
    super.postStop()
  }

  override def receive = {
    case extractor(records) => {
      consumer ! Confirm(records.offsets, commit = true)
      for (record <- records.recordsList) {
        println(s"Record: $record")
        table(record.topic()) ! Record(record.topic(), Option(record.key()), record.value())
      }
    }
    case SubscribeTopic(name, _) => {
      table(name) = sender()
    }
  }
}

package ai.scynet

import java.text.SimpleDateFormat
import java.util.Properties

import org.apache.kafka.streams.state._
import com.sksamuel.avro4s.{FromRecord, RecordFormat}
import io.confluent.kafka.streams.serdes.avro.{GenericAvroSerde, SpecificAvroSerde}
import org.apache.avro.generic.{GenericData, GenericDatumReader, GenericDatumWriter, GenericRecord}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serdes, Serializer}
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.scala._
import implicits._
import collection.JavaConverters._

object Main {
  case class LastBlock(blockNumer: String) // TODO: There should be a better way to cast primitives to GenericRecord ?


  import messages._
  var streams: KafkaStreams = null

  def main(args: Array[String]): Unit = {
    val genericAvro = new GenericAvroSerde()


    val config: Properties = {
      val p = new Properties()
      p.put(StreamsConfig.APPLICATION_ID_CONFIG, "lastSeen")
      val bootstrapServers = sys.env.getOrElse("BROKER", "127.0.0.1:9092")
      p.put(StreamsConfig.consumerPrefix(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG), "earliest")
      p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
      p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass.getName)
      p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass.getName)
      p.put("cleanup.policy", TopicConfig.CLEANUP_POLICY_COMPACT)
      p.put("segment.ms", "0")
      p.put("schema.registry.url", sys.env.getOrElse("SCHEMA_REGISTRY", "http://127.0.0.1:8081"))
      p
    }

    val builder = new StreamsBuilder()
    genericAvro.configure(config.asScala.asJava, false)

    implicit val consumed : Consumed[String, GenericRecord] = Consumed.`with`(Serdes.String(), genericAvro)
    implicit val consumedSS : Consumed[String, String] = Consumed.`with`(Serdes.String(), Serdes.String())
    implicit val serialized: Serialized[String, String] = Serialized.`with`(Serdes.String(), Serdes.String())
    //    implicit val materialized: Materialized[String, Array[Byte], ByteArrayKeyValueStore] = Materialized.as("balance_accounts")
    implicit val materializedSS: Materialized[String, String, ByteArrayKeyValueStore] = Materialized.as("lastSeen_accounts_SS")
    val materializedBlocks: Materialized[String, String, ByteArrayKeyValueStore] = Materialized.as("lastSeen_blocks")
    implicit val produced: Produced[String, String] = Produced.valueSerde(Serdes.String()).withKeySerde(Serdes.String())
    implicit val joined: Joined[String, String, GenericRecord] = Joined.`with`(Serdes.String(), Serdes.String(), genericAvro)
    implicit val joineds: Joined[String, String, String] = Joined.`with`(Serdes.String(), Serdes.String(), Serdes.String())

    materializedSS.withCachingDisabled()
    val df:SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")

    val stream = builder.stream[String, GenericRecord]("etherium_blocks")


    val formatBlock = RecordFormat[Block]

    builder.addStateStore(
      Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore("blocks"), Serdes.String(), genericAvro)
    )

    stream.flatMap((key, value) => {
      val block = formatBlock.from(value)

      // We join all the iterators to get all the traces from the block
      block.transactions.foldLeft( block.traces.toIterator )(_ ++ _.traces.iterator)
        .map(_.action).flatMap {
        case Call(_, from, _, _, to, _) => {
          Array(from, to)
        }
        case Reward(author, _, _) => {
          Array(author)
        }
        case Create(from, _, _, _) => {
          Array(from)
        }
        case Suicide(_, _, refund) => {
          Array(refund)
        }
      }.map((_, block.timeStamp)).toList
    }).mapValues(_.toString)
      .groupByKey
      .aggregate({ BigInt(0).toString() })(( key, value, previous ) => {
        var result = BigInt(previous) + BigInt(value)
        if(result  < 0){
          result = BigInt(0)
        }
        println(s"${key}: ${result}")
        result.toString
      })
      .toStream.to("lastSeen")

    streams = new KafkaStreams(builder.build(), config)


    sys.addShutdownHook({
      println(streams.state())
      streams.close()
      streams.cleanUp()
      println("Cleaned up")
    })



    streams.cleanUp()
    streams.start()
  }
}
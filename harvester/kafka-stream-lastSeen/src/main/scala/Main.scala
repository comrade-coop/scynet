package ai.scynet

import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.Properties

import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import com.sksamuel.avro4s.{FromRecord, RecordFormat, ToRecord}
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDe
import io.confluent.kafka.streams.serdes.avro.{GenericAvroSerde, SpecificAvroSerde}
import org.apache.avro.generic.{GenericData, GenericRecord}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.common.serialization.Serdes.StringSerde
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.processor.StreamPartitioner
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.FunctionConversions._
import org.apache.kafka.streams.scala.kstream.KTable
import implicits._
import org.apache.avro.util.Utf8

import collection.JavaConverters._




object Main {
  import messages._
  var view: ReadOnlyKeyValueStore[String, GenericRecord] = null
  var streams: KafkaStreams = null

  def main(args: Array[String]): Unit = {
//    val serde = Serdes.serdeFrom(new AvroSerializer, new AvroDeserializer)
    val genericAvro = new GenericAvroSerde()



    val config: Properties = {
      val p = new Properties()
      p.put(StreamsConfig.APPLICATION_ID_CONFIG, "lastSeen")
      val bootstrapServers = if (args.length > 0) args(0) else "192.168.1.188:9092"
      p.put(StreamsConfig.consumerPrefix(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG), "earliest")
      p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
      p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass.getName)
      p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass.getName)
      p.put("cleanup.policy", TopicConfig.CLEANUP_POLICY_COMPACT)
      p.put("segment.ms", "0")

      p.put("schema.registry.url", "http://192.168.1.188:8081")
      p
    }

    val builder = new StreamsBuilder()
    genericAvro.configure(config.asScala.asJava, false)

    implicit val consumed : Consumed[String, GenericRecord] = Consumed.`with`(Serdes.String(), genericAvro)
    implicit val consumedSS : Consumed[String, String] = Consumed.`with`(Serdes.String(), Serdes.String())
    implicit val serialized: Serialized[String, String] = Serialized.`with`(Serdes.String(), Serdes.String())
//    implicit val materialized: Materialized[String, Array[Byte], ByteArrayKeyValueStore] = Materialized.as("balance_accounts")
    implicit val materializedSS: Materialized[String, String, ByteArrayKeyValueStore] = Materialized.as("lastSeen_accounts_SS")
    implicit val produced: Produced[String, String] = Produced.valueSerde(Serdes.String()).withKeySerde(Serdes.String())
    implicit val joined: Joined[String, String, GenericRecord] = Joined.`with`(Serdes.String(), Serdes.String(), genericAvro)

//    materialized.withCachingDisabled()
    materializedSS.withCachingDisabled()
//    materialized.withLoggingEnabled(config.asScala.asJava)
    val df:SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")

//    val blockTable = builder.table[String, GenericRecord]("blocks", Materialized.as[String, GenericRecord, ByteArrayKeyValueStore]("block_table"))
    val blockStream = builder.stream[String, GenericRecord]("blocks")
    val traceStream = builder.stream[String, GenericRecord]("traces")

//    blockTable.toStream.foreach(println("block: ", _,_))

    val formatBlock = RecordFormat[Block]
    val formatTrace = RecordFormat[Trace]


    val acountForBlock = traceStream.flatMapValues((key, value) => {
      val trace = formatTrace.from(value)
      trace.action match {
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
      case user: User => Array[String]()
      }
    }).join(blockStream)((account, blockRecord) => {
      val block = formatBlock.from(blockRecord)

      (account, block.timeStamp)
      // TODO: migrate from timeDifferencs to block Window
    }, JoinWindows.of(250))
        .map((_, kv) => kv)
        .mapValues(_.toString)
        .groupByKey
        .aggregate({ "0" })((key, value, previous) => {
          (BigInt(value) max BigInt(previous)).toString
        })
        .toStream
      .mapValues(value => {
        println(df.format(BigInt(value).longValue() * 1000), value)
        BigInt(value).longValue().toString
      })
      .to("lastSeen")




//      .map((key ,value: String) => (value, key))
//      .groupByKey
//      .aggregate({ "0" })((key, value, previous) => {
//        if(view == null){
//          view = streams.store(blockTable.queryableStoreName, QueryableStoreTypes.keyValueStore[String, GenericRecord]())
//        }
//
//        if(view != null){
//          val block = (BigInt(value) max BigInt(previous)).toString()
//
//          println((key, block, Option(view.get(block)).map(formatBlock.from).map(_.timeStamp) ))
//
//        }
//        (BigInt(value) max BigInt(previous)).toString
//      })






//    (blockTable)((a,b) => (a, b), Windowed).map((key, value) => {
//      println(value)
//      (key, value.toString())
//    }).to("lastSeen")

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
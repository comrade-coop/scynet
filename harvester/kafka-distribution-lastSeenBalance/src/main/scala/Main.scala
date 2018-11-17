package ai.scynet

import java.text.SimpleDateFormat
import java.util.Properties

import org.apache.kafka.streams.state._
import com.sksamuel.avro4s.{AvroSchema, FromRecord, RecordFormat}
import io.confluent.kafka.streams.serdes.avro.{GenericAvroSerde, SpecificAvroSerde}
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serdes, Serializer}
import org.apache.kafka.streams.kstream.{Consumed, Materialized, Produced, Serialized}
import org.apache.kafka.streams.{KafkaStreams, KeyValue, StreamsConfig}
import org.codehaus.jackson.map.ObjectMapper

import scala.runtime.AbstractFunction2
//import org.apache.kafka.streams._
//import org.apache.kafka.streams._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._

import collection.JavaConverters._

object Main {
  val max0 = (Math.log10(100000000.0) / Math.log10(1.2)).toInt
  val max1 = (Math.log10(2592000.0*2*2*2) / Math.log10(1.2)).toInt

  var streams: KafkaStreams = null

  def main(args: Array[String]): Unit = {
    val genericAvro = new GenericAvroSerde()


    val config: Properties = {
      val p = new Properties()
      p.put(StreamsConfig.APPLICATION_ID_CONFIG, "distribution")
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

    val matrix = Array.ofDim[Double](max1, max0) // TODO: check if it is really double that we need, numpy says so, but I am not so sure.

    implicit val consumed : Consumed[String, String] = Consumed.`with`(Serdes.String(), Serdes.String())
    implicit val materialized: Materialized[String, (Int, Int), ByteArrayKeyValueStore] = Materialized.as("locations")
    implicit val produced: Produced[String, GenericRecord] = Produced.`with`(Serdes.String(), genericAvro)
    implicit val produced_avro: Produced[GenericRecord, GenericRecord] = Produced.`with`(genericAvro, genericAvro)
    implicit val serialized: Serialized[String, GenericRecord] = Serialized.`with`(Serdes.String(), genericAvro)

    val lastSeenTable = builder.table[String, String]("lastSeen")
    val balanceTable  = builder.table[String, String]("balance")

    implicit val format_value = RecordFormat[DataFrame]
    implicit val format_key = RecordFormat[Key]
    implicit val format_tuple = RecordFormat[(Int, Int)]

    implicit def typeToRecord[T](value: T)(implicit format: RecordFormat[T]): GenericRecord = format.to(value)
    implicit def recordToType[T](value: GenericRecord)(implicit format: RecordFormat[T]): T = format.from(value)


    val result = lastSeenTable.join(balanceTable)((lastSeenString: String, balanceString: String) => {
      //      val lastSeen = lastSeenString.toLong()
      val balance = BigInt(balanceString) / BigInt("100000000000000000" /* 0.1 ETH in wei */)

      //  val lastSeenIndex = (BigDecimalMath.log10(BigDecimal(lastSeen), MathContext(10)) / BigDecimal(Math.log10(1.2))).longValueExact()
      //  val balanceIndex = (BigDecimalMath.log10(BigDecimal(balance), MathContext(1000)) / BigDecimal(Math.log10(1.2))).longValueExact()
      val lastSeenIndex = Math.log10(lastSeenString.toDouble) / Math.log10(1.2)
      val balanceIndex =  Math.log10(balance.toDouble) / Math.log10(1.2)
      println((lastSeenIndex, balanceIndex))

      typeToRecord(Math.min(lastSeenIndex.round , max1 - 1).toInt, Math.min(balanceIndex.round, max0 - 1).toInt)
    })
      .toStream
      .groupByKey
      .aggregate(typeToRecord(-1 ,-1))((key: String, value_generic: GenericRecord, previous_generic: GenericRecord) => {
        val value: (Int, Int) = recordToType[(Int,Int)](value_generic) // I put the functions explicitly because somebody might think that these lines are redundant in the future.
        val previous: (Int, Int) = recordToType[(Int,Int)](previous_generic)
        println(value, previous)
        // We check if the values are bellow zero so we know the person is new to the matrix.
        if(previous._1 >= 0 || previous._2 >= 0) {
          matrix(previous._1)(previous._2) -= 1
        }


        matrix(value._1)(value._2) += 1
        value_generic
      })(Materialized.`with`(Serdes.String(), genericAvro)).toStream



    result.map[String, GenericRecord]((x: String, y: GenericRecord) => {
      println(matrix)
      ("", DataFrame(Seq(matrix.map(_.toSeq).toSeq)))
    }).to("disribution-lsb")

    result.map[String, GenericRecord]((x: String, y: GenericRecord) => {
      (x, y)
    }).to("distribution") // TODO: better names, and delte useless parts.

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

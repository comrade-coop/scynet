package ai.scynet

import java.math.BigInteger
import java.util.Properties

import com.sksamuel.avro4s.{FromRecord, RecordFormat, ToRecord}
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDe
import io.confluent.kafka.streams.serdes.avro.{GenericAvroSerde, SpecificAvroSerde}
import org.apache.avro.generic.{GenericData, GenericRecord}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.common.serialization.Serdes.StringSerde
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.apache.kafka.streams.kstream.{Consumed, Materialized, Produced, Serialized}
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
  def main(args: Array[String]): Unit = {
//    val serde = Serdes.serdeFrom(new AvroSerializer, new AvroDeserializer)
    val genericAvro = new GenericAvroSerde()



    val config: Properties = {
      val p = new Properties()
      p.put(StreamsConfig.APPLICATION_ID_CONFIG, "sample")
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
    implicit val serialized: Serialized[String, Array[Byte]] = Serialized.`with`(Serdes.String(), Serdes.ByteArray())
//    implicit val materialized: Materialized[String, Array[Byte], ByteArrayKeyValueStore] = Materialized.as("balance_accounts")
    implicit val materializedSS: Materialized[String, String, ByteArrayKeyValueStore] = Materialized.as("balance_accounts_SS")
    implicit val produced: Produced[String, String] = Produced.valueSerde(Serdes.String()).withKeySerde(Serdes.String())

//    materialized.withCachingDisabled()
    materializedSS.withCachingDisabled()
//    materialized.withLoggingEnabled(config.asScala.asJava)

    val stream = builder.stream[String, GenericRecord]("etherium_blocks")

    val format = RecordFormat[Block]

    val result = stream.flatMap((key, value) => {
      val block = format.from(value)

      block.transactions.foldLeft( block.traces.toIterator )(_ ++ _.traces.iterator)
        .map(_.action).flatMap {
        case call: Call => {
          Array((call.from, call.value.negate()), (call.to, call.value))
        }
        case reward: Reward => {
          Array((reward.author, reward.value))
        }
        case create: Create => {
          Array((create.from, create.value.negate()))
//          Array[(String, BigInteger)]()
        }
        case suicide: Suicide => {
          Array[(String, BigInteger)]()
        }
       }.toList
    }).mapValues(transfer => transfer.toByteArray)
      .groupByKey
      .aggregate({ BigInt(0).toString() })(( key, value, previous ) => {
        var result = BigInt(previous) + BigInt(value)
        if(result  < 0){
          result = BigInt(0)
        }
        println(s"${key}: ${result}")
        result.toString
      })
      .toStream.to("balance")




//    val result = textStream
//      .flatMapValues((value: GenericRecord) => {
////        println(value.getSchema)
////        println( format.to(Text(Coproduct[Data](Line(12, "Hello world")))).getSchema )
////        println()
////        val data: GenericRecord = value.get("data").asInstanceOf[GenericRecord]
//        format.from(value).productIterator.flatMap(_ match {
//          case Inl(Line(n, str)) => {
//            str.split(" ")
//          }
//          case Inr(Inl(Words(words))) => {
//            words
//          }
//        }).toIterable
//      })
//      .selectKey((key, value) => value)
//      .groupByKey
//      .count()
//      .toStream
//      .map((k,c) => {
//        println(k, c)
//        (k, c.toString)
//      })
//
//
//    result.to("word_count")

    val streams = new KafkaStreams(builder.build(), config)

    sys.addShutdownHook({
      streams.close()
      streams.cleanUp()
      println("Cleaned up")
    })



    streams.cleanUp()
    streams.start()
  }
}

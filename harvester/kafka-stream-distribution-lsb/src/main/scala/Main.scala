package ai.scynet

import java.math.{BigInteger, MathContext}
import java.text.SimpleDateFormat
import java.util.Properties

import ch.obermuhlner.math.big.BigDecimalMath
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
import java.math.BigDecimal
import java.util

import collection.JavaConverters._




object Main {
  val max0 = BigDecimalMath.log10(new BigDecimal( new BigInteger("100000000").multiply(new BigInteger("1000000000000000000") )) , new MathContext(10000)).divide(new BigDecimal(Math.log10(1.2))).longValueExact() //Max Bal for scale, in WEI
  val max1 = (BigDecimalMath.log10(new BigDecimal(2592000*2*2*2) , new MathContext(10000)).divide(new BigDecimal(Math.log10(1.2)))).longValueExact() //in seconds, or 30*2*2*2 days

  import messages._

  var streams: KafkaStreams = null

  def main(args: Array[String]): Unit = {
//    val serde = Serdes.serdeFrom(new AvroSerializer, new AvroDeserializer)
    val genericAvro = new GenericAvroSerde()

    val matrix: Array[Array[Long]] = new Array(max1.intValue()).map((a: Array[Long]) => new Array[Long](max0.intValue()))

    val config: Properties = {
      val p = new Properties()
      p.put(StreamsConfig.APPLICATION_ID_CONFIG, "distribution")
      val bootstrapServers = "192.168.1.188:9092"
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

    val lastSeenStream = builder.table[String, String]("lastSeen")
    val balanceStream = builder.table[String, String]("balances")

    //    Math.log10()



    lastSeenStream.join[String, String](balanceStream)((lastSeenString: String, balanceString: String) => {
      val lastSeen = new BigDecimal(new BigInteger(lastSeenString))
      val balance = new BigDecimal(new BigInteger(balanceString))

      println(Pair(lastSeen, balance))

      val lastSeenIndex = (BigDecimalMath.log10(lastSeen, new MathContext(1000)).divide(new BigDecimal(Math.log10(1.2)))).longValueExact()
      val balanceIndex = (BigDecimalMath.log10(balance, new MathContext(1000)).divide(new BigDecimal(Math.log10(1.2)))).longValueExact()

      (Math.min(lastSeenIndex, max1 - 1), Math.min(balanceIndex, max0 - 1))
    }, materializedSS).toStream()
      .groupByKey()



//    ({ (0, 0) }, (key, value, previous) => {
//        matrix[previous._0.toInt()][previous._1.toInt()]++
//        matrix[value._0.toInt()][value._1.toInt()]--
//
//        println("{")
//        for(row in matrix){
//          println( util.Arrays.toString(row) )
//        }
//        println("}")
//
//        value
//      }, Materialized.`as`("distribution")).toStream().to("distribution")

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


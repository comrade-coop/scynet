package ai.scynet

import java.math.BigInteger
import java.util.Properties

import collection.JavaConverters._
import org.apache.kafka.clients.producer._
import org.apache.avro.generic.GenericRecord
import org.web3j.protocol.parity.Parity
import ai.scynet.implicits._
import ai.scynet.messages._
import org.web3j.protocol.core.methods.response.EthBlock
import org.web3j.protocol.http.HttpService
import org.web3j.protocol.parity.methods.response.Trace
import org.web3j.protocol.parity.methods.response.Trace.{CallAction, CreateAction, RewardAction, SuicideAction}


// TODO: move these in a different file, presumably kept as avro files that can be reused for other projects.
// TODO: Check which field are nullable and make all of them Optional (it is possible to assume that all fields are nullable, but let's not do that. )
object messages {
  case class Block(hash: String, number: BigInteger, timeStamp: BigInteger, transactions: List[Transaction], traces: List[Trace])
  case class Transaction(receipt: Receipt, chainId: Long, creates: Option[String], from: String, gas: BigInteger, gasPrice: BigInteger,
                         hash: String, input: String, nonce: BigInteger, publicKey: String, to: Option[String], index: BigInteger, indexRaw: String, value: BigInteger, srv: SRV , traces: List[Trace])


  case class SRV(S: String, R: String, V: Long)

  case class Receipt(gasUsed: BigInteger, status: Option[String] /*, logs: List[Log] */ )
  case class Log()

  sealed trait Action
  case class Trace(action: Action, result: Option[Result], error: Option[String], subtracesCount: BigInteger, traceAdress: List[BigInteger], transactionHash: Option[String], transactionPosition: Option[BigInteger], `type`: String)

  case class Result(address: Option[String], code: Option[String], gasUsed: BigInteger, output: Option[String])

  case class Call(callType: String, from: String, gas: BigInteger, input: String, to: String, value: BigInteger) extends Action
  case class Reward(author: String, value: BigInteger, rewardType: String) extends Action
  case class Create(from: String, gas: BigInteger, init: String, value: BigInteger) extends Action
  case class Suicide(address: String, balance: BigInteger, refundAddress: String) extends Action
}

object Main {

  def transformTrace(trace: Trace) : messages.Trace = {
    val action = trace.getAction match {
      case call: CallAction => Call(call.getCallType, call.getFrom, call.getGas, call.getInput, call.getTo, call.getValue)
      case reward: RewardAction => Reward(reward.getAuthor, reward.getValue, reward.getRewardType)
      case create: CreateAction => Create(create.getFrom, create.getGas, create.getInit, create.getValue)
      case suicide: SuicideAction => Suicide(suicide.getAddress, suicide.getBalance, suicide.getRefundAddress)
    }

    // TR: Trace Result
    val result = Option(trace.getResult)
      .map(traceRsult => Result( Option(traceRsult.getAddress),  Option(traceRsult.getCode), traceRsult.getGasUsed, Option(traceRsult.getOutput)))

    val value = messages.Trace(action, result, Option(trace.getError), trace.getSubtraces, trace.getTraceAddress.asScala.toList, Option(trace.getTransactionHash), Option(trace.getTransactionPosition), trace.getType)
//    println(value)
    value
  }

  def main(args: Array[String]): Unit = {

    val properties = new Properties()
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, sys.env.getOrElse("BROKER", "127.0.0.1:9092") )
    properties.put("acks", "1")
    properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    properties.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer")
    properties.put("schema.registry.url", sys.env.getOrElse("SCHEMA_REGISTRY", "http://127.0.0.1:8081") )

    val producer = new KafkaProducer[String, GenericRecord](properties)

    // TODO: Use websockets it will be better/faster
    val web3j = Parity.build(new HttpService(sys.env.getOrElse("PARITY", "http://127.0.0.1:8545") ))
    web3j.catchUpToLatestAndSubscribeToNewBlocksObservable(50785, true).map[EthBlock.Block](blockResult => blockResult.getBlock).subscribe(block => {

      println(s"Getting transactions for block: ${block.getNumber}")

      // TODO: It is slow to query the traces 2 times, find a way to do it only once. (HashMaps for the win?)
      lazy val blockTraces = web3j.traceBlock(block.getNumber).send().getTraces.asScala.filter(trace => trace.getTransactionHash == null).map(transformTrace).toList

      lazy val transactions = block.getTransactions.asScala.map(r => {
        val t : EthBlock.TransactionObject = r.asInstanceOf[EthBlock.TransactionObject]

        println(s"transaction from: ${t.getFrom}")

        // TODO: Save Logs, because they are needed for the ERC20 tokens, and the FUTURE

        val receipt = web3j.ethGetTransactionReceipt(t.getHash).sendAsync().get().getTransactionReceipt.get()
        val traces = web3j.traceTransaction(t.getHash).send().getTraces.asScala.map(transformTrace).toList

        Transaction(
          Receipt(receipt.getGasUsed, Option(receipt.getStatus)),
          t.getChainId,
          Option(t.getCreates),
          t.getFrom,
          t.getGas,
          t.getGasPrice,
          t.getHash,
          t.getInput,
          t.getNonce,
          t.getPublicKey,
          Option(t.getTo),
          t.getTransactionIndex,
          t.getTransactionIndexRaw,
          t.getValue,
          SRV(t.getS, t.getR, t.getV),
          traces)
      }).toList

      val result = Block(block.getHash, block.getNumber, block.getTimestamp, transactions, blockTraces)
      val record = new ProducerRecord[String, GenericRecord]("etherium_blocks", block.getNumber.toString, result)

      producer.send(record, (metadata: RecordMetadata, exception: Exception) => {
        if(exception != null){
          println(s"error: ${exception}")
        }else{
          println(s"Finished producing block: ${block.getNumber}")
        }
      })

    })

    sys.addShutdownHook({
      producer.flush()
      producer.close()
      println("Cleaned up")
    })
  }
}

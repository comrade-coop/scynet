package ai.scynet

import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.util.Properties

import collection.JavaConverters._
import org.apache.kafka.clients.producer._
import com.sksamuel.avro4s._
import org.apache.avro.generic.GenericRecord
import org.web3j.protocol.ipc.UnixIpcService
import org.web3j.protocol.parity.Parity
import ai.scynet.implicits._
import ai.scynet.messages._
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.methods.response.EthBlock
import org.web3j.protocol.core.methods.response.EthBlock.{TransactionObject, TransactionResult}
import org.web3j.protocol.parity.methods.response.Trace
import org.web3j.protocol.parity.methods.response.Trace.{CallAction, CreateAction, RewardAction, SuicideAction}

object messages {
  case class Block(hash: String, number: BigInteger, timeStamp: BigInteger, transactions: List[String])
  case class Transaction(block: TransactionBlock, receipt: Receipt, chainId: Long, creates: Option[String], from: String, gas: BigInteger, gasPrice: BigInteger,
                         hash: String, input: String, nonce: BigInteger, publicKey: String, to: Option[String], index: BigInteger, indexRaw: String, value: BigInteger, srv: SRV /* , traces: List[Trace] */)
  case class TransactionBlock(hash: String, number: BigInteger)


  case class SRV(S: String, R: String, V: Long)

  case class Receipt(gasUsed: BigInteger, status: Option[String] /*, logs: List[Log] */ )
  case class Log()

  sealed trait Action
  case class Trace(action: Action, result: Option[Result], block: TransactionBlock, error: Option[String], subtracesCount: BigInteger, traceAdress: List[BigInteger], transactionHash: Option[String], transactionPosition: Option[BigInteger], `type`: String)

  case class Result(address: Option[String], code: Option[String], gasUsed: BigInteger, output: Option[String])

  case class Call(callType: String, from: String, gas: BigInteger, input: String, to: String, value: BigInteger) extends Action
  case class Reward(author: String, value: BigInteger, rewardType: String) extends Action
  case class Create(from: String, gas: BigInteger, init: String, value: BigInteger) extends Action
  case class Suicide(address: String, balance: BigInteger, refundAddress: String) extends Action
  case class User(from: String) extends Action

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

    val block = TransactionBlock(trace.getBlockHash, trace.getBlockNumber)

    val value = messages.Trace(action, result, block, Option(trace.getError), trace.getSubtraces, trace.getTraceAddress.asScala.toList, Option(trace.getTransactionHash), Option(trace.getTransactionPosition), trace.getType)
//    println(value)
    value
  }

  def main(args: Array[String]): Unit = {
    val properties = new Properties()
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.1.188:9092")
    //    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    //    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ser.getClass.getName)
    properties.put("acks", "1")
    properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    properties.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer")
    properties.put("schema.registry.url", "http://192.168.1.188:8081")

    val producer = new KafkaProducer[String, GenericRecord](properties)

    val web3j = Parity.build(new UnixIpcService("/run/media/alex4o/cbdd8c04-ebe5-4763-84e7-af1b584f79fd/eth_chain/jsonrpc.ipc"))

    web3j.catchUpToLatestAndSubscribeToNewBlocksObservable(50785, true).subscribe(blockResult => {
      val block = blockResult.getBlock
      println(s"Getting transactions for block: ${block.getNumber}")


      web3j.traceBlock(block.getNumber).send().getTraces.asScala.filter(trace => trace.getTransactionHash == null).map(transformTrace(_)).foreach(trace => {
        producer.send(new ProducerRecord[String, GenericRecord]("traces", trace.block.number.toString, trace))
      })

      block.getTransactions.forEach(r => {
        val t : EthBlock.TransactionObject = r.asInstanceOf[EthBlock.TransactionObject]
        val block = TransactionBlock(t.getBlockHash, t.getBlockNumber)

        println(s"transaction from: ${t.getFrom}")

        // TODO: Save Logs, because they are needed for the ERC20 tokens, and the FUTURE

        val receipt = web3j.ethGetTransactionReceipt(t.getHash).sendAsync().get().getTransactionReceipt.get()

        val transaction = Transaction(
          TransactionBlock(t.getBlockHash, t.getBlockNumber),
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
          SRV(t.getS, t.getR, t.getV))

        // TODO: Store both gas price and gas used
        val transactionTrace = messages.Trace(User(transaction.from), Option(Result(None, None, receipt.getGasUsed.multiply(t.getGasPrice), None)), block, None, 0, List(), Option(t.getHash), Option(t.getTransactionIndex), "Transaction" )

        // TODO: Make sending records look better
        producer.send(new ProducerRecord[String, GenericRecord]("traces", transactionTrace.block.number.toString, transactionTrace)).get()
        println("produced")
        val traces = web3j.traceTransaction(t.getHash).send().getTraces.asScala.map(transformTrace).foreach(trace => {
          producer.send(new ProducerRecord[String, GenericRecord]("traces", trace.block.number.toString, trace))
        })

//        println(transaction)
        val record = new ProducerRecord[String, GenericRecord]("transactions", transaction.hash, transaction)

        producer.send(record, (metadata: RecordMetadata, exception: Exception) => {
          if(exception != null){
            println(s"error: ${exception}")
          }
          println(s"Finished transaction for block: ${block.number}")
        })
      })

      val transactionHashes = block.getTransactions.asScala.map(transaction => transaction.asInstanceOf[TransactionObject].get()).map(transaction => transaction.getHash).toList
      producer.send(new ProducerRecord[String, GenericRecord]("blocks", block.getNumber.toString, Block(block.getHash, block.getNumber, block.getTimestamp, transactionHashes)))

    })

    sys.addShutdownHook({
      producer.flush()
      producer.close()

      println("Cleaned up")
    })

//    web3j.catchUpToLatestAndSubscribeToNewTransactionsObservable(0).subscribe(t => {
//
////      transaction.get
//      println(t.getTransactionIndexRaw)
//
//      val transaction = Transaction(
//         Block(t.getBlockHash, t.getBlockNumber),
//         t.getChainId,
//         t.getCreates,
//         t.getFrom,
//         t.getGas,
//         t.getGasPrice,
//         t.getHash,
//         t.getInput,
//         t.getNonce,
//         t.getPublicKey,
//         t.getTo,
//         t.getTransactionIndex,
//         t.getTransactionIndexRaw,
//         t.getValue)
//
//
//        producer.send(new ProducerRecord[String, GenericRecord]("transactions", transaction.indexRaw, transaction))
//    })



//      producer.send(new ProducerRecord[String, GenericRecord]("words", lineNumber.toString , Text( Coproduct[Data]( Line(12, line)) ) ))
//      producer.send(new ProducerRecord[String, GenericRecord]("words", lineNumber.toString , Text( Coproduct[Data]( Words(line.split(" "))) ) ))

  }
}
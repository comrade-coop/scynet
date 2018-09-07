package ai.scynet

import java.math.BigInteger
import implicits._

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
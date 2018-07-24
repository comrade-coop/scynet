package com.obecto.trading_bot_breeder

import spray.json._
import java.security.{MessageDigest}
import com.obecto.gattakka.genetics.{Genome}
import java.io.{File, PrintWriter}


object Utils {
  def mapGenomeToStringStrategy(genome: Genome): String = {
    import DefaultJsonProtocol._
    import Converter.AnyJsonProtocol._

    Converter.serialize(genome).toJson.compactPrint
  }

  def printToFile(f: File)(op: PrintWriter => Unit): Unit = {
    val p = new PrintWriter(f)
    try {
      op(p)
    } finally {
      p.close()
    }
  }

  def generateGenomeHash(genome: Genome): String = {
    val strategy = mapGenomeToStringStrategy(genome)
    MessageDigest.getInstance("MD5").digest(strategy.getBytes()).map("%02x".format(_)).mkString.substring(0, 10)
  }
}

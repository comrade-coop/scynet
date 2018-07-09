package com.obecto.trading_bot_breeder

import com.obecto.gattakka.{Individual}
import com.obecto.gattakka.genetics.{Genome}
import com.obecto.gattakka.messages.individual.{Initialize}
import java.io.{IOException, File, PrintWriter}
import java.security.{MessageDigest}
import spray.json._
import Math.{abs}

class CustomIndividualActor(genome: Genome) extends Individual(genome) {
  // import context.dispatcher

  var process: scala.sys.process.Process = null
  var stopping = false
  val strategy = mapGenomeToStringStrategy()
  // val shortHash = BigInt(strategy.hashCode()).abs.toString(16).padTo(6, '0').substring(0, 6)
  val shortHash = MessageDigest.getInstance("MD5").digest(strategy.getBytes()).map("%02x".format(_)).mkString.substring(0, 10)
  var startTime = 0l

  override def customReceive = {
    case Initialize(data) =>
      startProcess()
  }

  override def postStop(): Unit = {
    if (process != null) {
      stopping = true
      process.destroy()
    }
  }

  def dispatchFitness(fitness: Double, displayScore: Double, iterations: Int): Unit = {
    super.dispatchFitness(fitness)
    if (!fitness.isNaN && fitness != 0.0) {
      val endTime = System.currentTimeMillis / 1000
      val sign = if (displayScore > 0) 1 else 0
      printToFile(new File(f"../results/$sign${abs(displayScore)}%010.2f-${shortHash}/${abs(displayScore)}%06.2f-${shortHash}.txt")) { p =>
        p.println(s"fitness = $fitness")
        p.println(s"score = $displayScore")
        p.println(s"iterations = $iterations")
        p.println(s"time = ${endTime - startTime}s")
        p.println(s"hash = $shortHash")
        p.println(s"chromosome = $strategy")
      }
    }
  }

  private def printToFile(f: File)(op: PrintWriter => Unit): Unit = {
    val p = new PrintWriter(f)
    try { op(p) } finally { p.close() }
  }

  private def startProcess(): Unit = {
    import scala.sys.process._
    var errorText = ""
    var shouldPrintError = false
    println(f"Started $shortHash")
    startTime = System.currentTimeMillis / 1000

    val io = new ProcessIO(
      in => {
        try {
          in.write(strategy.toCharArray.map(_.toByte))
          in.close()
        } catch {
          case _: IOException => println("Failed to write to input")
        }
      },
      out => {
        val resultLines = scala.io.Source.fromInputStream(out).getLines.toList
        val resultMap = resultLines.view.map({line =>
          val parts = line.split("=", 2)
          if (parts.length == 2) Some((parts(0).trim, parts(1).trim))
          else None
        }).flatten.toMap
        out.close()

        println(f"Finished $shortHash")

        if (resultMap.contains("score")) {
          val score = resultMap.getOrElse("score", "0").toDouble
          val displayScore = resultMap.getOrElse("display_score", "0").toDouble
          val iterations = resultMap.getOrElse("iterations", "-1").toInt
          dispatchFitness(score, displayScore, iterations)
        } else if (!stopping) {
          dispatchFitness(Double.NaN, Double.NaN, -1)
          if (errorText == "") {
            shouldPrintError = true
          } else {
            printToFile(new File(f"../results/${shortHash}-error.txt")) { p =>
              p.println(s"chromosome = $strategy")
              p.println(errorText)
            }
          }
        }
      },
      err => {
        errorText = scala.io.Source.fromInputStream(err).mkString // The child usually dies if we don't wait for it to finish working
        if (shouldPrintError) {
          printToFile(new File(f"../results/${shortHash}-error.txt")) { p =>
            p.println(s"chromosome = $strategy")
            p.println(errorText)
          }
        }
        err.close()
      })

    process = Process(Main.commandToRun, new File("../")) run io
  }

  private def mapGenomeToStringStrategy(): String = {
    import DefaultJsonProtocol._
    import Converter.AnyJsonProtocol._

    Converter.serialize(genome).toJson.compactPrint
  }
}

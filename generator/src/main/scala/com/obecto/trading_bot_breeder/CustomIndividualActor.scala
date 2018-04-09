package com.obecto.trading_bot_breeder

import com.obecto.gattakka.Individual
import com.obecto.gattakka.genetics.{Genome}
import com.obecto.gattakka.messages.individual.Initialize
import spray.json._

class CustomIndividualActor(genome: Genome) extends Individual(genome) {
  // import context.dispatcher

  var process: scala.sys.process.Process = null
  var stopping = false

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

  private def startProcess(): Unit = {
    import scala.sys.process._
    import java.io.IOException
    val strategy = mapGenomeToStringStrategy()
    println(strategy.replaceAll("\n", ""))

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
        val result = scala.io.Source.fromInputStream(out).mkString
        val resultLines = result.split('\n')
        out.close()

        try {
          val scoreIndex = resultLines.lastIndexWhere(_.startsWith("score"))
          if (!stopping) {
            dispatchFitness(resultLines(scoreIndex).split("=")(1).toDouble)
          }
        } catch {
          case ex: Throwable => {
            if (!stopping) {
              println((ex, strategy.replaceAll("\n", "")))
              dispatchFitness(Double.NaN)
            }
          }
        }
      },
      err => {
        scala.io.Source.fromInputStream(err).mkString // The child usually dies if we don't wait for it to finish working
        err.close()
      })

    process = Process(Main.commandToRun, new java.io.File("../")) run io
  }

  private def mapGenomeToStringStrategy(): String = {
    val configOpt = genome.chromosomes.find(x => Descriptors.Configs.contains(x.descriptor))
    var config = configOpt.get.value.asInstanceOf[Map[Any, Any]]

    def fixLayers(layers: Seq[Map[Any, Any]]): Seq[Map[Any, Any]] = {
      var result = List[Map[Any, Any]]()
      var pending = Set[Map[Any, Any]]()

      def tryAddLayerToResult(layer: Map[Any, Any]): Unit = {
        val inputs = layer("inputs").asInstanceOf[List[Long]]
        if (inputs.size <= result.size) {

          val modifiedInputs = inputs.map(_ % result.size)
          val modifiedLayer = layer ++ Map("inputs" -> modifiedInputs)
          result = result :+ modifiedLayer

          pending = pending - layer
          pending.foreach(tryAddLayerToResult)
        } else {
          pending = pending + layer
        }
      }

      layers.foreach(tryAddLayerToResult)

      result
    }


    config = config ++ Map(
      "layers" -> fixLayers(genome.chromosomes.filterNot(_ == configOpt.get).map(_.value.asInstanceOf[Map[Any, Any]]))
    )

    import DefaultJsonProtocol._
    import Descriptors.AnyJsonProtocol._

    config.toJson.compactPrint
    //Descriptors.Descriptor(brain, genome.chromosomes.map(mapChromosomeToSignal).filter(_ != null)).toJson.compactPrint
  }
}

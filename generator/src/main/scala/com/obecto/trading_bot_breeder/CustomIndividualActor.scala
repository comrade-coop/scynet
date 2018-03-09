package com.obecto.trading_bot_breeder

import com.obecto.gattakka.Individual
import com.obecto.gattakka.genetics.{Genome}
import com.obecto.gattakka.messages.individual.Initialize
import spray.json._

class CustomIndividualActor(genome: Genome) extends Individual(genome) {
  // import context.dispatcher

  var process: scala.sys.process.Process = null

  override def customReceive = {
    case Initialize(data) =>
      startProcess()
  }

  override def postStop(): Unit = {
    if (process != null) {
      process.destroy()
    }
  }

  private def startProcess(): Unit = {
    import scala.sys.process._
    import java.io.{OutputStream, InputStream, IOException}
    val strategy = mapGenomeToStringStrategy()
    println(strategy.replaceAll("\n", ""))

    val io = new ProcessIO(
      (in: OutputStream) => {
        try {
          in.write(strategy.toCharArray.map(_.toByte))
          in.close()
        } catch {
          case _: IOException => println("Failed")
        }
        println("Did it")
      },
      (out: InputStream) => {
        var result = scala.io.Source.fromInputStream(out).mkString.trim
        println(result)
        result = result.split('\n').last
        out.close()
        if (result == "") {
          result = "0"
        }
        dispatchEvent(scala.util.Random.nextDouble())
        // dispatchEvent(result.toDouble)
      },
      _.close())

    process = Process(Seq("cat", "-"), new java.io.File("../")) run io
  }

  private def mapGenomeToStringStrategy(): String = {
    var config = genome.chromosomes.head.value.asInstanceOf[Map[Any, Any]]

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

      layers.foreach(tryAddLayerToResult(_))

      result
    }

    config = config ++ Map(
      "layers" -> fixLayers(genome.chromosomes.tail.map(_.value.asInstanceOf[Map[Any, Any]]))
    )

    import DefaultJsonProtocol._
    import Descriptors.AnyJsonProtocol._

    config.toJson.compactPrint
    //Descriptors.Descriptor(brain, genome.chromosomes.map(mapChromosomeToSignal).filter(_ != null)).toJson.compactPrint
  }
}

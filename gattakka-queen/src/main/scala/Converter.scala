package ai.scynet.queen

import com.obecto.gattakka.genetics._
import com.obecto.gattakka.genetics.descriptors._
import spray.json._


object Converter {

  object AnyJsonProtocol {
    implicit val AnyFormat = new JsonFormat[Any] {
      def write(thing: Any): JsValue = thing match {
        case d: Double => JsNumber(d)
        case l: Long => JsNumber(l)
        case i: Int => JsNumber(i)
        case s: String => JsString(s)
        case b: Boolean => JsBoolean(b)
        case o: Some[_] => write(o.get)
        case null => JsNull
        case None => JsNull
        case m: Map[_, _] => JsObject(m.map(x => (x._1.toString, write(x._2))))
        case a: Seq[_] => JsArray(a.view.map(write(_)).toVector)
        case _ => JsObject()
      }

      def read(value: JsValue): Any = value match {
        case JsNumber(n) => {
          if (n.isValidInt) n.toInt
          else if (n.isValidLong) n.toLong
          else n.toDouble
        }
        case JsString(s) => s
        case JsBoolean(b) => b
        case JsNull => null
        case JsArray(a) => a.map(read(_)).toVector
        case JsObject(o) => o.map(x => (x._1, read(x._2))).toMap
      }
    }
  }

  def serialize(genome: Genome, useStack: Boolean = true): Map[Any, Any] = {
    val configOpt = genome.chromosomes.find(x => Descriptors.Configs.contains(x.descriptor))
    var config = configOpt.get.value.asInstanceOf[Map[Any, Any]]

    def fixLayers(layers: Seq[Map[Any, Any]]): Seq[Map[Any, Any]] = {
      var result = List[Map[Any, Any]]()
      var pending = Set[Map[Any, Any]]()
      var stack = List[Int]()

      def tryAddLayerToResult(layer: Map[Any, Any]): Unit = {
        val inputs = layer("inputs").asInstanceOf[List[Long]]
        if (inputs.size <= stack.size && useStack) {

          val modifiedInputs = stack.take(inputs.size)
          stack = stack.drop(inputs.size)

          if (layer contains "special") {
            if (layer("special") == "duplicate") {
              stack = stack :+ modifiedInputs(0)
              stack = stack :+ modifiedInputs(0)
            } else if (layer("special") == "swap") {
              stack = stack :+ modifiedInputs(1)
              stack = stack :+ modifiedInputs(0)
            }
          } else {
            val modifiedLayer = layer ++ Map("inputs" -> modifiedInputs)
            stack = stack :+ result.size
            result = result :+ modifiedLayer
          }

          pending = pending - layer
          pending.foreach(tryAddLayerToResult)
        } else if (inputs.size <= result.size && !useStack) {

          if (!layer.contains("special")) {
            val modifiedInputs = inputs.map(_ % result.size)
            val modifiedLayer = layer ++ Map("inputs" -> modifiedInputs)
            result = result :+ modifiedLayer
          }

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

    config
  }

  def deserialize(config: Map[Any, Any]): Genome = {
    def matchChromosome(result: Any, descriptors: Iterable[GeneDescriptor]): Option[Chromosome] = {
      val chromosome = descriptors
        .map(matchGeneDescriptor(result, _))
        .find(_.isDefined).map(_.get)
        .map(gene => Chromosome(gene.toByteArray, gene.descriptor))
      if (chromosome.isEmpty) {
        println(result)
      }
      chromosome
    }

    def matchGeneDescriptor(result: Any, descriptor: GeneDescriptor): Option[Gene] = {
      Option(descriptor match {
        case descriptor: DoubleGeneDescriptor =>
          if (result.isInstanceOf[Double])
            descriptor(result.asInstanceOf[Double])
          else if (result.isInstanceOf[Int])
            descriptor(result.asInstanceOf[Int].toDouble)
          else if (result.isInstanceOf[Long])
            descriptor(result.asInstanceOf[Long].toDouble)
          else null

        case descriptor: LongGeneDescriptor =>
          if (result.isInstanceOf[Int])
            descriptor(result.asInstanceOf[Int].toLong)
          else if (result.isInstanceOf[Long])
            descriptor(result.asInstanceOf[Long])
          else null

        case d: EnumGeneDescriptor[Any] =>
          val index = d.values.indexOf(result)
          if (index == -1) null else d(index)

        case d: GeneGroupDescriptor if (result.isInstanceOf[Iterable[Any]]) =>
          val value = result.asInstanceOf[Iterable[Any]]
          val submatches = (value.toList, d.geneDescriptors).zipped.map(matchGeneDescriptor)
          if (submatches contains None) null else GeneGroup(submatches.map(_.get), d)

        case d: MapGeneGroupDescriptor if (result.isInstanceOf[Map[Any, Any]]) =>
          val value = result.asInstanceOf[Map[Any, Any]]
          val submatches = d.geneDescriptors.map({
            case (key, desc) =>
              if (value contains key) {
                matchGeneDescriptor(value(key), desc)
              } else {
                Some(desc())
              }
          })
          // println(submatches.zip(d.geneDescriptors).map(x => x._2._1.toString + "->" + (x._1 != None).toString).mkString(", "))
          if (submatches contains None) null else MapGeneGroup(submatches.map(_.get), d)
        case _ => null
      })
    }

    Genome(
      matchChromosome(config, Descriptors.Configs).get +:
        config("layers").asInstanceOf[Vector[Any]].view.flatMap(x => matchChromosome(x, Descriptors.Layers.map(_._2))).toList
    )
  }
}

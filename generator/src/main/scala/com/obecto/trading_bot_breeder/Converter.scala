package com.obecto.trading_bot_breeder

import com.obecto.gattakka.genetics._
import com.obecto.gattakka.genetics.descriptors._

object Converter {

  def serialize(genome: Genome): Map[Any, Any] = {
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

    config
  }

  def deserialize(config: Map[Any, Any]): Genome = {
    def matchChromosome(result: Any, descriptors: Iterable[GeneDescriptor]): Option[Chromosome] = {
      descriptors
        .map(matchGeneDescriptor(result, _))
        .find(_.isDefined).map(_.get)
        .map(gene => Chromosome(gene.toByteArray, gene.descriptor))
    }

    def matchGeneDescriptor(result: Any, descriptor: GeneDescriptor): Option[Gene] = {
      Option(descriptor match {
        case descriptor: DoubleGeneDescriptor if (result.isInstanceOf[Double]) =>
          descriptor(result.asInstanceOf[Double])

        case descriptor: LongGeneDescriptor if (result.isInstanceOf[Int]) =>
          descriptor(result.asInstanceOf[Int].toLong)
        case descriptor: LongGeneDescriptor if (result.isInstanceOf[Long]) =>
          descriptor(result.asInstanceOf[Long])

        case d: EnumGeneDescriptor[Any] =>
          d.values.find(x => x == result).map(value => d(value)).orNull

        case d: GeneGroupDescriptor if (result.isInstanceOf[Iterable[Any]]) =>
          val value = result.asInstanceOf[Iterable[Any]]
          val submatches = (value.toList, d.geneDescriptors).zipped.map(matchGeneDescriptor)
          if (submatches contains None) null else GeneGroup(submatches.map(_.get), d)

        case d: MapGeneGroupDescriptor if (result.isInstanceOf[Map[Any, Any]]) =>
          val value = result.asInstanceOf[Map[Any, Any]]
          val submatches = d.geneDescriptors.map({case (key, desc) => matchGeneDescriptor(value(key), desc)})
          if (submatches contains None) null else MapGeneGroup(submatches.map(_.get), d)
        case _ => null
      })
    }

    Genome(
      matchChromosome(config, Descriptors.Configs).get +:
      config("layers").asInstanceOf[List[Any]].flatMap(x => matchChromosome(x, Descriptors.Configs))
    )
  }
}

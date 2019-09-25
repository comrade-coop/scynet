package ai.scynet.queen

import akka.actor.ActorRef
import akka.actor.{ActorSystem, Props}
import com.obecto.gattakka.genetics.operators._
import com.obecto.gattakka.genetics.descriptors.GeneDescriptor
import com.obecto.gattakka.genetics.{Chromosome, Genome}
import com.obecto.gattakka.{IndividualDescriptor, Pipeline, PipelineOperator, Population}

import scala.io.Source
import scala.util.Random
import com.obecto.gattakka.messages.population.RefreshPopulation
import spray.json.DefaultJsonProtocol

class GattakaQueenHelper {
  def evaluator = classOf[QueenEvaluator]

  def individualActor = classOf[CustomIndividualActor]

  def refreshPopulation(actor: ActorRef): Unit = {
    actor ! RefreshPopulation()
  }

  def generateRandomChromosome(descriptors: Traversable[(Double, GeneDescriptor)]): () => Chromosome = {
    val totalWeigth = descriptors.view.map(_._1).sum
    () => {
      var left = Random.nextDouble * totalWeigth
      if (descriptors.size == 1) {
        descriptors.head._2.createChromosome()
      } else {
        descriptors.find(x => {left -= x._1; left <= 0.0}).get._2.createChromosome()
      }
    }
  }

  val generateRandomInput: () => Chromosome = generateRandomChromosome(Descriptors.InputLayers)
  val generateRandomNonInputLayer: () => Chromosome = generateRandomChromosome(Descriptors.NonInputLayers)
  val generateRandomOutputLayer: () => Chromosome = generateRandomChromosome(Descriptors.OutputLayers)

  import java.nio.file.{Paths, Files}
  var initialChromosomesTemp: List[Genome] = null


  if (Files.exists(Paths.get(f"currentPopulation.txt"))) {
    println("Reading input from file...")
    val inputGenomes = for (line <- Source.fromFile(f"currentPopulation.txt").getLines) yield {
      import spray.json._
      import DefaultJsonProtocol._
      import Converter.AnyJsonProtocol._

      val genome = Converter.deserialize(line.parseJson.convertTo[Map[Any, Any]])

      genome
    }
    initialChromosomesTemp = inputGenomes.toList
  } else {
    initialChromosomesTemp = (1 to 1).map((i: Int) => {
      new Genome(List(
        Descriptors.AdamConfig.createChromosome(),
        generateRandomInput()
      ) ++ (1 to (Random.nextInt(4) + 1)).map(x => generateRandomNonInputLayer())
        ++ List(generateRandomOutputLayer())
      )
    }).toList
//    for (chromosome <- initialChromosomesTemp) {
//      import DefaultJsonProtocol._
//      import Converter.AnyJsonProtocol._
//      import spray.json._
//
//      val genome = Converter.serialize(chromosome).toJson.compactPrint
//      val deserialized = Converter.deserialize(genome.parseJson.convertTo[Map[Any, Any]])
//      val reserialized = Converter.serialize(deserialized).toJson.compactPrint
//      if(genome != reserialized) {
//        println(genome)
//        println(reserialized)
//        assert(false)
//      }
//    }
  }

  val initialChromosomes: List[Genome] = initialChromosomesTemp



  val pipelineOperators: List[PipelineOperator] = List(
    new PipelineOperator {
      override def apply(snapshot: List[IndividualDescriptor]): List[IndividualDescriptor] = {
        import DefaultJsonProtocol._
        import Converter.AnyJsonProtocol._
        import spray.json._
        import java.io.{IOException, File, PrintWriter}

        val p = new PrintWriter(new File(f"currentPopulation.txt"))

        try {
          for (descriptor <- snapshot) {
            val genome = Converter.serialize(descriptor.genome).toJson.compactPrint
//            println(s"$genome")
            p.println(s"$genome")
          }
        } finally {
          p.close()
        }
        snapshot
      }
    },
    // new PipelineOperator {
    //   def apply(descriptors: List[IndividualDescriptor]): List[IndividualDescriptor] = {
    //     descriptors filter (!_.fitness.isNaN)
    //   }
    // },
    new EliteOperator {
      val elitePercentage = 0.2
    },
    new UniformCrossoverReplicationOperator {
      val replicationChance = 0.1
      override val keepFirstChildOnly = true
      val parentSelectionStrategy = new TournamentSelectionStrategy(8)
    },
    new BinaryMutationOperator {
      override def killParent = false
      val mutationChance = 0.1
      val bitFlipChance = 0.05
    },
    new InsertMutationOperator {
      val mutationChance = 0.2
      val insertionChance = 0.1
      def createChromosome() = generateRandomNonInputLayer()
      override def apply(genome: Genome): Genome = {
        if (genome.chromosomes.size < 20) {
          super.apply(genome)
        } else {
          genome
        }
      }
    },
    new DropMutationOperator {
      val mutationChance = 0.15
      val dropChance = 0.1
      override def mayDrop(chromosome: Chromosome): Boolean = !Descriptors.Configs.contains(chromosome.descriptor)
    },
    new PipelineOperator with MutationBaseOperator {
      val mutationChance = 0.15
      val transmuteChance = 0.1
      def apply(genome: Genome): Genome = {
        new Genome(genome.chromosomes.map { chromosome =>
          if (rnd.nextFloat() < transmuteChance) apply(chromosome) else chromosome
        })
      }
      val groups = List(Descriptors.ActivationLayers, Descriptors.MergeLayers, Descriptors.InputLayers)
      val generators = groups.map(generateRandomChromosome)
      def apply(chromosome: Chromosome): Chromosome = {
        var result = chromosome
        for ((group, generator) <- groups.zip(generators) if (group contains chromosome)) {
          result = generator()
        }
        result
      }
    },
    new ShuffleMutationOperator {
      val mutationChance = 0.1
    },
    new DeduplicationOperator {},
    new LimitSizeOperator {
      val targetPopulationSize = 20
    }
  )
}

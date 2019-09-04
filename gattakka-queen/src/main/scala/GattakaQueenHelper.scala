package ai.scynet.queen

import akka.actor.ActorRef
import akka.actor.{ActorSystem, Props}
import com.obecto.gattakka.genetics.operators._
import com.obecto.gattakka.genetics.descriptors.{GeneDescriptor}
import com.obecto.gattakka.genetics.{Chromosome, Genome}
import com.obecto.gattakka.{Pipeline, PipelineOperator, Population}
import scala.io.Source

import scala.util.Random
import com.obecto.gattakka.messages.population.RefreshPopulation

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
      descriptors.find(x => {left -= x._1; left <= 0.0}).get._2.createChromosome()
    }
  }

  val generateRandomInput: () => Chromosome = generateRandomChromosome(Descriptors.InputLayers)
  val generateRandomNonInputLayer: () => Chromosome = generateRandomChromosome(Descriptors.NonInputLayers)
  val generateRandomOutputLayer: () => Chromosome = generateRandomChromosome(Descriptors.OutputLayers)

  val initialChromosomes: List[Genome] = (1 to 20).map((i: Int) => {
    new Genome(List(
      Descriptors.AdamConfig.createChromosome(),
      generateRandomInput()
    ) ++ (1 to (Random.nextInt(4) + 1)).map(x => generateRandomNonInputLayer())
      ++ List(generateRandomOutputLayer())
    )
  }).toList

  val pipelineOperators: List[PipelineOperator] = List(
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

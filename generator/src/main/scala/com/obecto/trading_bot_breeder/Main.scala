package com.obecto.trading_bot_breeder

import akka.actor.{ActorSystem, Props}
import com.obecto.gattakka.genetics.operators._
import com.obecto.gattakka.genetics.descriptors.{GeneDescriptor}
import com.obecto.gattakka.genetics.{Chromosome, Genome}
import com.obecto.gattakka.{Pipeline, PipelineOperator, Population}

import scala.util.Random

object Main extends App {
  implicit val system = ActorSystem("gattakka")
  implicit val executionContext = system.dispatcher

  def generateRandomChromosome(descriptors: Seq[GeneDescriptor]): Chromosome = {
    descriptors(Random.nextInt(descriptors.size)).createChromosome()
  }

  val initialChromosomes = (1 to 20).map((i: Int) => {
    new Genome(List(
      Descriptors.AdamConfig.createChromosome(),
      generateRandomChromosome(Descriptors.InputLayers)
    ) ++ (3 to Random.nextInt(10)).map(x => generateRandomChromosome(Descriptors.Layers)))
  }).toList

  val pipelineOperators: List[PipelineOperator] = List(
    new EliteOperator {
      val elitePercentage = 0.2
    },
    new UniformCrossoverReplicationOperator {
      val replicationChance = 0.1
      override val keepFirstChildOnly = true
      val parentSelectionStrategy = new TournamentSelectionStrategy(8)
    },
    new BinaryMutationOperator {
      val mutationChance = 0.1
      val bitFlipChance = 0.05
    },
    new InsertMutationOperator {
      def createChromosome() = generateRandomChromosome(Descriptors.Layers)
      val insertionChance = 0.1
      val mutationChance = 0.1
    },
    new DropMutationOperator {
      override def mayDrop(chromosome: Chromosome): Boolean = !Descriptors.Configs.exists(chromosome == _)
      val dropChance = 0.1
      val mutationChance = 0.1
    },
    new DeduplicationOperator {},
    new LimitSizeOperator {
      val targetPopulationSize = 50
    }
  )
  val pipelineActor = system.actorOf(Pipeline.props(pipelineOperators))

  val evaluator = system.actorOf(Props(classOf[CustomEvaluator], classOf[CustomEvaluationAgent], ""), "evaluator")
  val populationActor = system.actorOf(Population.props(
    classOf[CustomIndividualActor],
    initialChromosomes,
    evaluator,
    pipelineActor
  ), "population")

}

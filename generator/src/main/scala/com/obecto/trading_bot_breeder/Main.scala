package com.obecto.trading_bot_breeder

import akka.actor.{ActorSystem, Props}
import com.obecto.gattakka.genetics.operators._
import com.obecto.gattakka.genetics.descriptors.{GeneDescriptor}
import com.obecto.gattakka.genetics.{Chromosome, Genome}
import com.obecto.gattakka.{Pipeline, PipelineOperator, Population, IndividualDescriptor}
import com.obecto.gattakka.messages.population.RefreshPopulation

import scala.util.Random

object Main extends App {
  val commandToRun = args.partition(_ == "--")._2

  if (commandToRun.length < 1) {
    println("Please give a command to run")
    println("Something like `sbt \"run -- myexec myparams\"`")
    println("The parameters JSON will then be passed over stdin to your executable")
  } else {
    implicit val system = ActorSystem("gattakka")

    def generateRandomChromosome(descriptors: Seq[GeneDescriptor]): Chromosome = {
      descriptors(Random.nextInt(descriptors.size)).createChromosome()
    }

    val initialChromosomes = (1 to 20).map((i: Int) => {
      new Genome(List(
        Descriptors.AdamConfig.createChromosome()
      ) ++ (1 to (Random.nextInt(3) + 1)).map(x => generateRandomChromosome(Descriptors.InputLayers)))
      // ) ++ (1 to Random.nextInt(2)).map(x => generateRandomChromosome(Descriptors.Layers)))
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
        val mutationChance = 0.1
        val bitFlipChance = 0.05
      },
      new InsertMutationOperator {
        val mutationChance = 0.1
        def createChromosome() = generateRandomChromosome(Descriptors.Layers)
        val insertionChance = 0.1
      },
      new DropMutationOperator {
        val mutationChance = 0.1
        override def mayDrop(chromosome: Chromosome): Boolean = !Descriptors.Configs.contains(chromosome.descriptor)
        val dropChance = 0.1
      },
      new DeduplicationOperator {},
      new LimitSizeOperator {
        val targetPopulationSize = 20
      }
    )

    val pipelineActor = system.actorOf(Pipeline.props(pipelineOperators))

    val evaluator = system.actorOf(Props(classOf[CustomEvaluator]), "evaluator")
    val populationActor = system.actorOf(Population.props(
      classOf[CustomIndividualActor],
      initialChromosomes,
      evaluator,
      pipelineActor
    ), "population")

  }
}

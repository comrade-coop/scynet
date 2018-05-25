package com.obecto.trading_bot_breeder

import akka.actor.{ActorSystem, Props}
import com.obecto.gattakka.genetics.operators._
import com.obecto.gattakka.genetics.descriptors.{GeneDescriptor}
import com.obecto.gattakka.genetics.{Chromosome, Genome}
import com.obecto.gattakka.{Pipeline, PipelineOperator, Population}

import scala.util.Random

object Main extends App {
  val commandToRun = args.partition(_ == "--")._2

  if (commandToRun.length < 1) {
    println("Please give a command to run")
    println("Something like `sbt \"run -- myexec myparams\"`")
    println("The parameters JSON will then be passed over stdin to your executable")
  } else {
    implicit val system = ActorSystem("gattakka")

    def generateRandomChromosome(descriptors: Traversable[(Double, GeneDescriptor)]): () => Chromosome = {
      val totalWeigth = descriptors.view.map(_._1).sum
      () => {
        var left = Random.nextDouble * totalWeigth
        descriptors.find(x => {left -= x._1; left <= 0.0}).get._2.createChromosome()
      }
    }

    val generateRandomInput = generateRandomChromosome(Descriptors.InputLayers)
    val generateRandomLayer = generateRandomChromosome(Descriptors.Layers)

    val initialChromosomes = (1 to 20).map((i: Int) => {
      new Genome(List(
        Descriptors.AdamConfig.createChromosome(),
        generateRandomInput()
      ) ++ (1 to (Random.nextInt(4) + 1)).map(x => generateRandomLayer()))
      // ) ++ (1 to Random.nextInt(2)).map(x => generateRandomLayer(Descriptors.Layers)))
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
        val mutationChance = 0.2
        def createChromosome() = generateRandomLayer()
        override def apply(genome: Genome): Genome = {
          if (genome.chromosomes.size < 20) {
            super.apply(genome)
          } else {
            genome
          }
        }
        val insertionChance = 0.1
      },
      new DropMutationOperator {
        val mutationChance = 0.15
        override def mayDrop(chromosome: Chromosome): Boolean = !Descriptors.Configs.contains(chromosome.descriptor)
        val dropChance = 0.1
      },
      new ShuffleMutationOperator {
        val mutationChance = 0.1
      },
      new DeduplicationOperator {},
      new LimitSizeOperator {
        val targetPopulationSize = 20
      }
    )

    val pipelineActor = system.actorOf(Pipeline.props(pipelineOperators))

    val evaluator = system.actorOf(Props(classOf[CustomEvaluator]), "evaluator")
    system.actorOf(Population.props(
      classOf[CustomIndividualActor],
      initialChromosomes,
      evaluator,
      pipelineActor
    ), "population")
  }
}

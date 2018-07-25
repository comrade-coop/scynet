package com.obecto.trading_bot_breeder

import akka.actor.{ActorSystem, Props, CoordinatedShutdown}
import akka.util.Timeout
import akka.pattern.ask
import akka.Done
import scala.concurrent.duration._
import com.obecto.gattakka.genetics.operators._
import com.obecto.gattakka.genetics.descriptors.{GeneDescriptor}
import com.obecto.gattakka.genetics.{Chromosome, Genome}
import com.obecto.gattakka.{Pipeline, PipelineOperator, Population}
import scala.io.Source
import com.obecto.gattakka.messages.population._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random
import java.io.{File}

object Main extends App {

  val doRecovery = args contains "recovery"

  val argParts = args.splitAt(args indexOf "--")
  val commandToRun = argParts._2.tail
  val filesToRead = if (!doRecovery) argParts._1 else getRecoveryFiles()

  def getRecoveryFiles(): Array[String] = {
    val recoveryDir = new File("../recovery")
    if (recoveryDir.exists() && recoveryDir.isDirectory) {
      val recoveryFiles = recoveryDir.listFiles().map(_.getPath)
      recoveryFiles
    } else {
      new Array[String](0)
    }
  }

  if (commandToRun.length < 1) {
    println("Please give a command to run")
    println("Something like `sbt \"run -- myexec myparams\"`")
    println("The parameters JSON will then be passed over stdin to your executable")
  } else if (filesToRead.length < 1) {
    println("No initial genome files specified")
  } else {
    val inputGenomes = for (file <- filesToRead) yield {
      import spray.json._
      import DefaultJsonProtocol._
      import Converter.AnyJsonProtocol._

      val contents = Source.fromFile(file).mkString
      Converter.deserialize(contents.parseJson.convertTo[Map[Any, Any]])
    }

    implicit val system = ActorSystem("gattakka")

    def generateRandomChromosome(descriptors: Traversable[(Double, GeneDescriptor)]): () => Chromosome = {
      val totalWeight = descriptors.view.map(_._1).sum
      () => {
        var left = Random.nextDouble * totalWeight
        descriptors.find(x => {
          left -= x._1;
          left <= 0.0
        }).get._2.createChromosome()
      }
    }

    val generateRandomInput = generateRandomChromosome(Descriptors.InputLayers)
    val generateRandomNonInputLayer = generateRandomChromosome(Descriptors.NonInputLayers)

    val initialChromosomes = inputGenomes.toList

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
        val replicationChance = 0.7
        override val keepFirstChildOnly = false
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
      new DiversitySelectionOperator {
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

    CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, "Sheny") {
      () =>
        println(s"\nexiting generator")
        implicit val timeout = Timeout(100 seconds)
        val future = populationActor ? GetCurrentPopulation
        future.mapTo[List[Genome]].map(result => {
          println()
          for (genome <- result) {
            val genomeStr = Utils.mapGenomeToStringStrategy(genome)
            val genomeHash = Utils.generateGenomeHash(genome)

            val dirName = "../recovery"
            val dir = new File(dirName)
            if (!dir.exists) {
              dir.mkdir()
            }

            Utils.printToFile(new File(f"$dirName/genome-$genomeHash.txt")) { p =>
              p.println(s"$genomeStr")
            }
          }
          Done
        })
    }
  }
}

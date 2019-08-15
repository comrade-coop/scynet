import ai.scynet.common.registry.*
import ai.scynet.core.common.registry.IgniteRegistry
import ai.scynet.core.configurations.ProcessorConfiguration
import ai.scynet.core.processors.*
import io.kotlintest.Spec
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.StringSpec
import kotlinx.coroutines.delay
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import java.util.*
import kotlin.random.Random

class ProcessorFactoryTest : StringSpec() {


	lateinit var koin: Koin
	lateinit var factory: ProcessorFactory
	lateinit var processorConfiguration: ProcessorConfiguration
	lateinit var secondProcessorConfiguration: ProcessorConfiguration
	lateinit var processorConfigurations: MutableList<ProcessorConfiguration>

	override fun beforeSpec(spec: Spec) {
		koin = startKoin {
			printLogger()
			modules(module {
				single<Ignite> { Ignition.start(IgniteConfiguration()) }
				single(named("streamRegistry")) {
					IgniteRegistry<String, Stream>("streamRegistry")
				} bind Registry::class
			})
		}.koin

		factory = ProcessorFactory()


		processorConfiguration = ProcessorConfiguration(
				inputs = mutableListOf("Big","Problem"),
				problem = "Problem",
				processorClass = BasicProcessor::class,
				properties = Properties())
		secondProcessorConfiguration = ProcessorConfiguration(
				inputs = mutableListOf("Small","Problem"),
				problem = "Small Problem",
				processorClass = BasicProcessor::class,
				properties = Properties())
		processorConfigurations = mutableListOf<ProcessorConfiguration>(processorConfiguration, secondProcessorConfiguration)
	}

	override fun afterSpec(spec: Spec) {
		koin.get<Ignite>().close()
		stopKoin()
	}

	init {

		"Instantiate ProcessorFactory consistently" {
			(factory.ignite is Ignite) shouldBe true
			(factory.state is IgniteCache<String, String>) shouldBe true
			(factory.registry is IgniteRegistry<String, Stream>) shouldBe true
		}

		"Create a BasicProcessor consistently" {

			var inputs: MutableList<String> = mutableListOf()
			var properties = Properties()

			// TODO:
			var testRegistry = IgniteRegistry<String, Stream>("streamRegistry")
			for (i in 0 until Random.nextInt(1,20)) {
				testRegistry.put(
						"stream$i",
						IgniteStream(
								"stream$i",
								"localhost:332$i",
								"StockPricePrediction",
								Properties()
						)
				)
				// TODO: UNCOMMENT OTHER COMMENTED BY ME TESTS PLS DONT FORGET, DISCUSS THE IGNITESTREAM() REFACTOR
				inputs.add("stream$i")
			}

			var processorConfig = ProcessorConfiguration("StockPricePrediction", BasicProcessor::class, inputs, properties)
			var processor: Processor = factory.create(processorConfig)

			println(processor)
		}

		"Test create multiple processors"{
			var registry = IgniteRegistry<String, Stream>("streamRegistry")
			val streams = mutableListOf("Big","Small","Problem")
			for(i in 0 until streams.size){
				registry.put(
						"${streams[i]}",
						IgniteStream(
								"${streams[i]}",
								"localhost:332$i",
								"${streams[i]}",
								Properties()))
			}
			val processors = factory.create(processorConfigurations)
			processors.size shouldBe 2

			val p1 = processors[0]
			val p2 = processors[1]

			p1.id shouldNotBe p2.id

			var isMatching = p1.descriptor.problem.equals("Problem")
			isMatching shouldBe true

			isMatching = p2.descriptor.problem.equals("Small Problem")
			isMatching shouldBe true
		}

		"Create BacsicConsumerProcessor" {
			//            var registry = IgniteRegistry<String, Stream>("streamRegistry")
//
			var basicProcessorConfiguration = ProcessorConfiguration(
					inputs = mutableListOf(),
					problem = "analyze ipsum",
					processorClass = BasicConsumerProcessor::class,
					properties = Properties())

			val processor: Processor = factory.create(basicProcessorConfiguration)
			val stream = processor.outputStream

			stream.listen { key: String, value: String, old: String? ->
				println("$key: $old -> $value")
			}

			processor.start()

			delay(5000)

			processor.stop()
			println("End")
		}

	}
}

import ai.scynet.common.registry.*
import ai.scynet.core.configurations.ProcessorConfiguration
import ai.scynet.core.processors.*
import io.kotlintest.Spec
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.StringSpec
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.util.*
import kotlin.random.Random

class ProcessorFactoryTest : StringSpec() {
	lateinit var koin: Koin

	override fun beforeSpec(spec: Spec) {
		koin = startKoin {
			printLogger()
			modules(module {
				single<Ignite> { Ignition.start(IgniteConfiguration()) }
				single { IgniteRegistry<String, Stream>("igniteRegistry") }
			})
		}.koin
	}

	override fun afterSpec(spec: Spec) {
		koin.get<Ignite>().close()
		stopKoin()
	}

	init {
		"Instantiate ProcessorFactory consistently" {
			var factory = ProcessorFactory()
			(factory.ignite is Ignite) shouldBe true
			(factory.state is IgniteCache<String, String>) shouldBe true
			(factory.registry is IgniteRegistry<String, Stream>) shouldBe true

			factory.registry.name shouldBe "igniteRegistry"
		}

		"Create a BasicProcessor consistently" {
			var factory: ProcessorFactory = ProcessorFactory()

			var inputs: MutableList<String> = mutableListOf()
			var properties = Properties()

			// TODO:

			for (i in 0 until Random.nextInt(1,20)) {
				IgniteRegistry<String, Stream>("igniteRegistry").put("stream$i", IgniteStream("stream$i", "localhost:3342", "StockPricePrediction"))
				// TODO: UNCOMMENT OTHER COMMENTED BY ME TESTS PLS DONT FORGET, DISCUSS THE IGNITESTREAM() REFACTOR
				inputs.add("stream$i")
			}

			var processorConfig = ProcessorConfiguration("StockPricePrediction", BasicProcessor::class, inputs, properties)
			var processor: Processor = factory.create(processorConfig)
			println(processor)
		}
	}
}

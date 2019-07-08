import ai.scynet.common.registry.IgniteRegistry
import io.kotlintest.Spec
import io.kotlintest.matchers.numerics.shouldBeLessThan
import io.kotlintest.properties.Gen
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.koin.dsl.module
import java.util.logging.LogManager

class IgniteRegistryTest : StringSpec() {
	lateinit var ignite: Ignite

	override fun beforeSpec(spec: Spec) {
		var koin = startKoin {
			printLogger()
			modules(module {
				single<Ignite> { Ignition.start(IgniteConfiguration()) }
			})
		}
	}

	override fun afterSpec(spec: Spec) {
		stopKoin()
		Ignition.stop(true)
	}

	init {
		"create Ignite Registry consistently" {
			IgniteRegistry<String, String>(Gen.string().nextPrintableString(8))
		}

		"put/get into Ignite Registry" {
			val registry = IgniteRegistry<String, String>(Gen.string().nextPrintableString(8))
			registry.put("hello", "world")
			registry.get("hello") shouldBe "world"
			println("result: ${registry.get("hello")}")
		}

		"query the Ignite Registry" {
			val registry = IgniteRegistry<String, String>(Gen.string().nextPrintableString(8))
			registry.put("hello", "1")
			registry.put("world", "2")
			registry.put("people", "3")

			var count = 0

			registry.query({ k,v -> true }, { k,v ->
				println("$k: $v")
				count shouldBeLessThan 4
				count += 1
				if(count == 4) {
					v shouldBe count.toString()
				}
			})

			registry.put("people", "4")


		}
	}
}

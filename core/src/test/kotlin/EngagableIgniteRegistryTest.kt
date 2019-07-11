import ai.scynet.common.registry.EngagableIgniteRegistry
import io.kotlintest.Spec
import io.kotlintest.matchers.numerics.shouldBeLessThan
import io.kotlintest.properties.Gen
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class EngagableIgniteRegistryTest: StringSpec() {
	lateinit var koin: Koin

	override fun beforeSpec(spec: Spec) {
		koin = startKoin {
			printLogger()
			modules(module {
				single<Ignite> { Ignition.start(IgniteConfiguration()) }
			})
		}.koin
	}

	override fun afterSpec(spec: Spec) {
		koin.get<Ignite>().close()
		stopKoin()
	}

	init {
		"create Engageable Ignite Registry consistently" {
			EngagableIgniteRegistry<String, TestClass>(Gen.string().nextPrintableString(8))
		}

		"put/get into Ignite Registry" {
			val registry = EngagableIgniteRegistry<String, TestClass>(Gen.string().nextPrintableString(8))

			registry.put("hello", TestClass(12))
			registry.get("hello")!!.id shouldBe 12
			println("result: ${registry.get("hello")}")
		}

		"query the Engagable Ignite Registry" {
			val registry = EngagableIgniteRegistry<String, TestClass>(Gen.string().nextPrintableString(8))
			registry.put("hello", TestClass(1))
			registry.put("world", TestClass(2))
			registry.put("people", TestClass(3))

			var count = 0

			registry.query({ k, v -> v.id % 2 == 1 }, { k, v ->
				println("$k: $v")
				count shouldBeLessThan 3
				count += 1
				if (count == 3) {
					v.id % 2 shouldBe 1
				}
			})

			registry.put("people", TestClass(4))
			registry.put("other", TestClass(5))
		}

		"engage/disengage classes" {
			val registry = EngagableIgniteRegistry<String, TestClass>(Gen.string().nextPrintableString(8))
			registry.put("hello", TestClass(1))

			registry.engage("hello")

			registry.disengage("hello")
		}

		"double disengage classes" {
			val registry = EngagableIgniteRegistry<String, TestClass>(Gen.string().nextPrintableString(8))
			registry.put("hello", TestClass(1))
			registry.engage("hello")
			registry.disengage("hello")
			shouldThrow<java.lang.Error> {
				registry.disengage("hello")
			}
			registry.engage("hello")
			registry.engage("hello")
			registry.disengage("hello")
			registry.disengage("hello")

		}
	}
}

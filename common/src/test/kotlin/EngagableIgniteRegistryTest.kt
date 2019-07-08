import ai.scynet.common.registry.EngagableIgniteRegistry
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
import org.koin.dsl.module

class EngagableIgniteRegistryTest: StringSpec() {
	override fun beforeSpec(spec: Spec) {
		startKoin {
			printLogger()
			modules(module {
				single<Ignite> { Ignition.start(IgniteConfiguration()) }
			})
		}
	}

	override fun afterSpec(spec: Spec) {
		stopKoin()
	}

	init {
		"create Engageable Ignite Registry consistently" {
			EngagableIgniteRegistry<String, TestClass>(Gen.string().random().first())
		}

		"put/get into Ignite Registry" {
			val registry = EngagableIgniteRegistry<String, TestClass>(Gen.string().random().first())

			registry.put("hello", TestClass(12))
			registry.get("hello")!!.id shouldBe 12
			println("result: ${registry.get("hello")}")
		}

		"query the Engagable Ignite Registry" {
			val registry = EngagableIgniteRegistry<String, TestClass>(Gen.string().random().first())
			registry.put("hello", TestClass(1))
			registry.put("world", TestClass(2))
			registry.put("people", TestClass(3))

			var count = 0

			registry.query({ k, v -> v.id % 2 == 1 }, { k, v ->
				println("$k: $v")
				count shouldBeLessThan 3
				count += 1
				if (count == 4) {
					v.id % 2 shouldBe 1
				}
			})

			registry.put("people", TestClass(4))
			registry.put("other", TestClass(5))
		}

		"engage/disengage classes" {
			val registry = EngagableIgniteRegistry<String, TestClass>(Gen.string().random().first())
			registry.put("hello", TestClass(1))

			registry.engage("hello")
			registry.disengage("hello")
		}
	}
}

import ai.scynet.core.processors.IgniteStream
import io.kotlintest.Spec
import io.kotlintest.specs.StringSpec
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.util.*

class StreamTest : StringSpec() {
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
		"Test if the ignite stream works" {
			val stream = IgniteStream("slow", "localhost:6969", "StockPrediction", Properties())

			val l: (String, Int) -> Unit = { k,v ->
				println(k)
			}

			stream.listen(l)

			stream.append("Hello", 12)
			stream.append("World", 13)
			stream.append("Scynet", 14)
		}

	}

}

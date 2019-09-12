package ai.scynet.queen

import ai.scynet.protocol.TrainingJob
import descriptors.Properties
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.koin.core.context.startKoin
import org.koin.dsl.module
import processors.ILazyStreamFactory
import processors.LazyStreamFactory
import java.util.*

fun main(args: Array<String>) {
	val cfg = IgniteConfiguration()
	cfg.igniteInstanceName = "HarvesterTest"
	//cfg.setPeerClassLoadingEnabled(true)
	val ignite = Ignition.start(cfg)

	startKoin {
		printLogger()
		modules(module {
			single<Ignite> { ignite }
		})
	}
	val LAZY_STREAM_FACTORY = "lazyStreamFactory"

	val GattakkaStreamID = UUID.randomUUID()
	println("\nGattakkaStreamID -> $GattakkaStreamID\n")
	val GattakkaStream = GattakkaLazyStream(GattakkaStreamID, null, Properties())

	ignite.services().deployClusterSingleton(LAZY_STREAM_FACTORY, LazyStreamFactory())
	//Register streams
	val factory = ignite.services().serviceProxy(LAZY_STREAM_FACTORY, ILazyStreamFactory::class.java, false)
	factory.registerStream(GattakkaStream)

	var streamProxy = factory.getInstance(GattakkaStreamID)
	var cursor =  streamProxy.listen { t:Long, c: TrainingJob, _ ->
		println("\nStream Output for $t -> $c\n")
	}

	System.`in`.reader().read()

	cursor.close()
	streamProxy.dispose() // TODO: It would be nice if it is possible to close all the cursor from the proxy, when disposing


}

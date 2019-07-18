package ai.scynet.core


import ai.scynet.common.registry.IgniteRegistry
import ai.scynet.core.configurations.ConfigurationHost
import ai.scynet.core.processors.BasicProcessor
import ai.scynet.core.processors.Processor
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun main(args: Array<String>) {
	val host = ConfigurationHost()
	val config = host.getIgniteConfiguration("DSLconfig.kts")

	startKoin {
		printLogger()
		modules(module {
			single<Ignite> { Ignition.start(config) }
		})
	}

	var stream = IgniteRegistry<String, Processor>("StreamRegistry")

	stream.put("hello0", BasicProcessor())
	stream.query({ _,_ -> true }, { key, value -> println("$key: $value") })

	stream.put("hello", BasicProcessor())
	stream.put("hello1", BasicProcessor())
	stream.put("hello2", BasicProcessor())

	println("Hello world")

}



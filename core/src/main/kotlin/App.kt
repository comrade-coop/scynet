package ai.scynet.core

import ai.scynet.core.common.registry.IgniteRegistry
import ai.scynet.core.configurations.ConfigurationHost
import ai.scynet.core.processors.BasicProcessor
import ai.scynet.core.processors.Processor
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun main(args: Array<String>) {
	val host = ConfigurationHost()
	val processorConfigurations = host.getProcessorConfigurations("processors.kts")
	val config = host.getIgniteConfiguration("ignite.kts")

	println(processorConfigurations)
	println(config)


    startKoin {
        printLogger()
        modules(module {
            single<Ignite> { Ignition.start(config) }
        })
    }

    var stream = IgniteRegistry<String, Processor>("StreamRegistry")

    stream.put("hello0", BasicProcessor())
    val cursor = stream.query({ k,_ -> k.length > 3 }, { key, value -> println("Callback result -> $key: $value") })
    stream.put("hello", BasicProcessor())
    stream.put("hello1", BasicProcessor())
    stream.put("hello2", BasicProcessor())
    stream.put("he", BasicProcessor())

    println("Hello world")

    stream.put("helloAgain", BasicProcessor())
    cursor.close()

}
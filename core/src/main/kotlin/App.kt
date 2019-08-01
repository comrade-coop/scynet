package ai.scynet.core

import ai.scynet.common.registry.IgniteRegistry
import ai.scynet.core.configurations.ConfigurationHost
import ai.scynet.core.processors.BasicProcessor
import ai.scynet.core.processors.Processor
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.koin.core.context.startKoin
import org.koin.dsl.module
import kotlin.system.exitProcess

fun main(args: Array<String>) {
	val host = ConfigurationHost()
	val processorConfigurations = host.getProcessorConfiguration("processors.kts")
	val config = host.getIgniteConfiguration("ignite.kts")

	println(processorConfigurations)
	println(config)

	exitProcess(0);

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
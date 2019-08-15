package ai.scynet.core

import ai.scynet.common.registry.Registry
import ai.scynet.core.common.registry.IgniteRegistry
import ai.scynet.core.configurations.ConfigurationHost
import ai.scynet.core.processors.*
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import java.util.*

fun main(args: Array<String>) {
    startKoin {
        printLogger()
        modules(module {
            single<Ignite> { Ignition.start() }
            single(named("streamRegistry")) {
                IgniteRegistry<String, Stream>("streamRegistry")
            } bind Registry::class
        })
    }

    val host = ConfigurationHost()
    val processorConfigurations = host.getProcessorConfigurations("DSLTwoProcessorsExample.kts")
    val registry = IgniteRegistry<String, Stream>("streamRegistry")
    registry.put("WordCount", IgniteStream("WordCount", "localhost", "wordcount", Properties()))
    val processorFactory = ProcessorFactory()
    val processors = processorFactory.create(processorConfigurations)
    val basicConsumerProcessor = processors.get(0)
    val basicProcessor = processors.get(1)

    basicProcessor.start()
    basicConsumerProcessor.start()
}


package ai.scynet.core

import ai.scynet.common.registry.Registry
import ai.scynet.core.processors.IgniteProcessor
import ai.scynet.core.processors.Processor

import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.Ignition
import org.apache.ignite.cache.query.ScanQuery
import org.koin.core.Koin
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.inject
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import registry.IgniteRegistry
import javax.script.ScriptEngineManager

/**

 * The first function to be executed
 * @param args The arguments we are receiving
 */
fun main(args: Array<String>) {
    var s = startKoin {
        printLogger()
        modules(module {
            single<Ignite> { Ignition.start() }
        })
    }

    var stream = IgniteRegistry<String, Processor>("StreamRegistry")


    stream.put("hello0", IgniteProcessor())

    stream.query({ _,_ -> true }, { key, value -> println("$key: $value") })

    stream.put("hello", IgniteProcessor())
    stream.put("hello1", IgniteProcessor())
    stream.put("hello2", IgniteProcessor())




    println("Hello world")

    var mgr = ScriptEngineManager()

    var engine = mgr.getEngineByExtension("kts")

    engine.eval("""
		println("Hello world")
    """.trimIndent())
}



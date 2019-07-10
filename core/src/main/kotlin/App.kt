package ai.scynet.core

import ai.scynet.common.registry.IgniteRegistry

import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.util.*
import javax.script.ScriptEngineManager

/**

 * The first function to be executed
 * @param args The arguments we are receiving
 */
fun main(args: Array<String>) {

	startKoin {
		printLogger()
		modules(module {
			single<Ignite> { Ignition.start() }
		})
	}

	println("Hello world")

	var mgr = ScriptEngineManager()

	var engine = mgr.getEngineByExtension("kts")

	engine.eval("""
		println("Hello world")
    """.trimIndent())
}



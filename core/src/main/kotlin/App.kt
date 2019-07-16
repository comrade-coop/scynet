package ai.scynet.core


import ai.scynet.common.registry.IgniteRegistry
import ai.scynet.core.processors.BasicProcessor
import ai.scynet.core.processors.IgniteProcessor
import ai.scynet.core.processors.Processor
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.io.FileReader
import java.util.*
import javax.script.ScriptEngineManager

/**

 * The first function to be executed
 * @param args The arguments we are receiving
 */
fun main(args: Array<String>) {

	var mgr = ScriptEngineManager()

	var engine = mgr.getEngineByExtension("kts")

	engine.eval("""
		println("Hello world")
    """.trimIndent())

 	val config = engine.eval(FileReader("src/main/kotlin/configurations/DSLconfig.kts"))

	startKoin {
		printLogger()
		modules(module {
			single<Ignite> { Ignition.start(config as IgniteConfiguration) }
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



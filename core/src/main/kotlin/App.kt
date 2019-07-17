package ai.scynet.core


import ai.scynet.common.registry.IgniteRegistry
import ai.scynet.core.configurations.Base
import ai.scynet.core.processors.BasicProcessor
import ai.scynet.core.processors.Processor
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.koin.core.context.startKoin
import org.koin.dsl.module

import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.*
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.JvmScriptCompiler
import kotlin.system.exitProcess
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
/**

 * The first function to be executed
 * @param args The arguments we are receiving
 */


fun main(args: Array<String>) {

//	var mgr = ScriptEngineManager()

//	var engine = mgr.getEngineByExtension("kts")

	val compiler = JvmScriptCompiler()
	val evaluator = BasicJvmScriptEvaluator()
	val host = BasicJvmScriptingHost(compiler = compiler, evaluator = evaluator)

	var compilationConfiguration = ScriptCompilationConfiguration {
		fileExtension("kts")

		jvm {
			baseClass(Base::class)
			dependenciesFromCurrentContext(wholeClasspath = true)
		}
	}

	val config = host.eval(ClassLoader.getSystemClassLoader().getResourceAsStream("DSLconfig.kts").reader().readText().toScriptSource(), compilationConfiguration, null)

	println(Base.igniteConfiguration)
	exitProcess(0)

	startKoin {
		printLogger()
		modules(module {
			single<Ignite> { Ignition.start(Base.igniteConfiguration) }
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



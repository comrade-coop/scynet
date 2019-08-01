package ai.scynet.core.configurations

import org.apache.ignite.configuration.IgniteConfiguration
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.baseClass
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.fileExtension
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.BasicJvmScriptEvaluator
import kotlin.script.experimental.jvm.dependenciesFromClassloader
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.JvmScriptCompiler

class ConfigurationHost {
   private val compiler = JvmScriptCompiler()
   private val evaluator = BasicJvmScriptEvaluator()
   private val host = BasicJvmScriptingHost(compiler = compiler, evaluator = evaluator)
   private val compilationConfiguration = ScriptCompilationConfiguration {
        fileExtension("kts")

        defaultImports.append(
                "ai.scynet.core.processors.*",
                "java.util.*"
        )

        jvm {
            baseClass(ConfigurationBase::class)
            dependenciesFromClassloader(wholeClasspath = true, classLoader = ClassLoader.getSystemClassLoader())
        }
    }

    fun getIgniteConfiguration(scriptName: String): IgniteConfiguration{
        val result = host.eval(ClassLoader.getSystemClassLoader().getResourceAsStream(scriptName).reader().readText().toScriptSource(), compilationConfiguration, null)
        if(result.reports.isNotEmpty()){
            result.reports.forEach(::println)
        }
        return ConfigurationBase.igniteConfiguration
    }

    fun getProcessorConfigurations(scriptName: String): MutableList<ProcessorConfiguration>{
        val result = host.eval(ClassLoader.getSystemClassLoader().getResourceAsStream(scriptName).reader().readText().toScriptSource(), compilationConfiguration, null)
        if(result.reports.isNotEmpty()){
            result.reports.forEach(::println)
        }
        return ConfigurationBase.processorConfigurations
    }
}
package configurations

import ai.scynet.core.configurations.ProcessorConfiguration
import ai.scynet.core.processors.BasicProcessor
import ai.scynet.core.processors.Processor
import java.util.*
import kotlin.reflect.KClass

class ProcessorConfigurationBuilder{
    private val configurations = ArrayList<ProcessorConfiguration>()

    var problem: String = ""
    var processorClass: KClass<out Processor> = BasicProcessor::class
    var inputs: MutableList<String> = ArrayList<String>()
    var properties: Properties = Properties()



    fun build(): MutableList<ProcessorConfiguration>{
        return configurations
    }

    fun processor(lambda: ProcessorConfigurationBuilder.() -> Unit){
        configurations.add(ProcessorConfigurationBuilder().apply(lambda).configure())
    }

    fun configure(): ProcessorConfiguration = ProcessorConfiguration(problem, processorClass, inputs, properties)
}



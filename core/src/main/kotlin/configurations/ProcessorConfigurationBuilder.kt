package ai.scynet.core.configurations

import ai.scynet.core.processors.BasicProcessor
import ai.scynet.core.processors.Processor
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

class ProcessorConfigurationBuilder{
//    public ProcessorConfigurationBuilder(){}
    private val configurations = ArrayList<ProcessorConfiguration>()

    var problem: String = ""
    var processorClass: KClass<out Processor> = BasicProcessor::class
    var inputs: MutableList<String> = ArrayList<String>()
    var properties: Properties = Properties()



    fun build(): ArrayList<ProcessorConfiguration>{
        return configurations
    }

    fun processor(lambda: ProcessorConfigurationBuilder.() -> Unit){
        configurations.add(ProcessorConfigurationBuilder().apply(lambda).configure())
    }

    fun configure(): ProcessorConfiguration = ProcessorConfiguration(problem, processorClass, inputs, properties)
}



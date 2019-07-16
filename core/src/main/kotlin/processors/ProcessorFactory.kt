package ai.scynet.core.processors

import ai.scynet.common.registry.IgniteRegistry
import ai.scynet.core.annotations.Inputs
import ai.scynet.core.processors.Stream
import ai.scynet.common.registry.*
import ai.scynet.core.configurations.ProcessorConfiguration
import ai.scynet.core.descriptors.ProcessorDescriptor
import ai.scynet.core.descriptors.StreamDescriptor
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation


class ProcessorFactory: KoinComponent {

	val ignite: Ignite by inject()
	val registry: Registry<String, Stream> by inject(named("streamRegistry"))
	var state: IgniteCache<String, String> = ignite.getOrCreateCache("processorFactoryState")

	init {
		//this.state!! = ignite.getOrCreateCache("PF:${uri.query.id}")
		println("Initializing processor factory.")
		println("stub")
	}

	fun create(processorConfigurations: MutableList<ProcessorConfiguration>): MutableList<Processor>{
		val processors = ArrayList<Processor>()
		for(configuration in processorConfigurations){
			processors.add(create(configuration))
		}
		return processors
	}

	fun create(processorConfiguration: ProcessorConfiguration): Processor {
		// TODO: Finish discussing the ProcessorConfiguration
		println("stub")
		val processor: Processor = processorConfiguration.processorClass.createInstance() // TODO: Create a simple processor that implements the interface
		processor.id = UUID.randomUUID()

		processor.inputStreams = mutableListOf()

		var inputStreamDescriptors: MutableList<StreamDescriptor> = mutableListOf()

		for ((i, input) in processorConfiguration.inputs.withIndex()) {
			val stream: Stream = registry.get(input)!!

			//if (processorConfiguration.processorClass::class.findAnnotation<Inputs>() !== null) {
				// TODO: Deny processor creation (Research annotation queries)
				// To finish implementing this we need to
				// discuss the semantics around the Type class and
				// Typization
			//}

			processor.inputStreams.add(stream)
			inputStreamDescriptors.add(stream.descriptor)
		}

		processor.descriptor = ProcessorDescriptor(
			processor.id,
			processor::class,
			processorConfiguration.problem,
			processorConfiguration.properties,
			inputStreamDescriptors,
			processor.outputStream.descriptor // Output is built runtime so this won't work 100%, but it's just pseudo-code for now
		)

		// processor.engage() should be invoked in processor.process()
		// processor.process() should initialize processor.outputStream
		return processor
	}

	fun list() {
		// TODO: Return created processors and their status -> Possibly useful. Not high priority
		println("stub")
	}
}
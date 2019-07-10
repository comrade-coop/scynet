package ai.scynet.core.processors

import ai.scynet.core.common.Stream
import ai.scynet.core.configurations.ProcessorConfiguration
import ai.scynet.core.descriptors.ProcessorDescriptor
import ai.scynet.core.descriptors.StreamDescriptor
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.reflect.full.createInstance


class ProcessorFactory: KoinComponent {

	val ignite: Ignite by inject()
	lateinit var state: IgniteCache<String,String>

	init {
		//this.state!! = ignite.getOrCreateCache("PF:${uri.query.id}")
		println("Initializing processor factory.")
		println("stub")
	}

	fun create(processorConfiguration: ProcessorConfiguration): Processor {
		// TODO: Finish discussing the ProcessorConfiguration
		println("stub")
		/*
			Query all processorConfiguration.inputs from the stream registry.
			Get their types and compare them with the annotation and assert if wrong

			scratch pad:
				findAnnotation class
				if (processorConfiguration.processorClass::class.annotations == processorConfiguration.inputs)

			Engage consumer_processor when attached
		*/
		val processor: Processor = processorConfiguration.processorClass.createInstance() // TODO: Create a simple processor that implements the interface
		processor.id = UUID.randomUUID()

		processor.inputStreams = listOf()

		var inputStreamDescriptors: List<StreamDescriptor> = listOf()

		for (input in processorConfiguration.inputs) {
			TODO("Implement")
			// query the input from the stream registry
			// queriedInput
			// processor.inputStreams.add(queriedInput)
			// inputStreamDescriptors.add(queriedInput.descriptor)
		}

		processor.descriptor = ProcessorDescriptor(
			processor.id,
			processor::class,
			processorConfiguration.problem,
			processorConfiguration.properties,
			inputStreamDescriptors,
			processor.outputStream.descriptor // Output is built runtime so this won't work 100%, but it's just pseudo-code for now
		)

		// TODO: Implement
		// processorRegistry.add(processor.descriptor)
		// processorRegistry.get(id).engage()


		return processor
	}

	fun list() {
		// TODO: Return created processors and their status -> Possibly useful. Not high priority
		println("stub")
	}
}
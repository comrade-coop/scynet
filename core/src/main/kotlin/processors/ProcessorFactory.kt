package ai.scynet.core.processors

import ai.scynet.core.descriptors.StreamDescriptor
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache

class ProcessorFactory {

	var state: IgniteCache<String,String>? = null

	init {
		//this.state!! = ignite.getOrCreateCache("PF:${uri.query.id}")
		println("Initializing processor factory.")
		println("stub")
	}

	fun create(streamDescriptor: StreamDescriptor): Processor {
		// TODO: Finish discussing the StreamDescriptor and then implement parsing.
		println("stub")

		val created: Processor? = null // TODO: Create a simple processor that implements the interface
		return created!!
	}

	fun list() {
		// TODO: Return created processors and their status -> Possibly useful. Not high priority
		println("stub")
	}
}
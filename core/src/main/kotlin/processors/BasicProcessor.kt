package ai.scynet.core.processors

import ai.scynet.core.descriptors.ProcessorDescriptor
import ai.scynet.core.descriptors.StreamDescriptor
import org.apache.ignite.IgniteDataStreamer
import org.apache.ignite.compute.ComputeTaskContinuousMapper

class BasicProcessor: Processor {
	/*
		A very basic processor, which implements the core processor interface.
		Optional constructor -> Initialize all data automatically
	*/

	constructor(descriptor: ProcessorDescriptor)

	var inputStreams: IgniteDataStreamer<String, String>? = null
	var outputStream: ComputeTaskContinuousMapper? = null
	var streamId: String = ""

	override var engageCount: Int
		get() = TODO("not implemented")
		set(value) {}

	override var descriptor: ProcessorDescriptor
		get() = TODO("not implemented")
		set(value) {}

	override fun process(): ComputeTaskContinuousMapper {
		TODO("not implemented")
	}

	override var id: String
		get() = TODO("not implemented")
		set(value) {}

	init {
		this.engageCount = 0
	}

}
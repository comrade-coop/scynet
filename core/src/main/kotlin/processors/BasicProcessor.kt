package ai.scynet.core.processors

import ai.scynet.core.annotations.Inputs
import ai.scynet.core.annotations.Type
import ai.scynet.core.common.Stream
import ai.scynet.core.descriptors.ProcessorDescriptor
import ai.scynet.core.descriptors.StreamDescriptor
import org.apache.ignite.IgniteDataStreamer
import org.apache.ignite.compute.ComputeTaskContinuousMapper
import java.util.*

@Inputs([Type("Int"), Type("String"), Type("String")])
class BasicProcessor: Processor {
	/*
		A very basic processor, which implements the core processor interface.
	*/


	override lateinit var inputStreams: List<Stream>
	override lateinit var outputStream: Stream
	override lateinit var descriptor: ProcessorDescriptor
	override lateinit var id: UUID

	override fun process(): ComputeTaskContinuousMapper {
		TODO("not implemented")
	}

}

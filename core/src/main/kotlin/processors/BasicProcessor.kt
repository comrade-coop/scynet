package ai.scynet.core.processors

import ai.scynet.core.annotations.Inputs
import ai.scynet.core.annotations.Output
import ai.scynet.core.annotations.Type
import ai.scynet.core.processors.Stream
import ai.scynet.core.descriptors.ProcessorDescriptor
import ai.scynet.core.descriptors.StreamDescriptor
import org.apache.ignite.IgniteDataStreamer
import org.apache.ignite.compute.ComputeTaskContinuousMapper
import java.util.*

@Inputs([Type("Int"), Type("String"), Type("String")])
@Output(Type("Tensor"))
class BasicProcessor: Processor {
	/*
		A very basic processor, which implements the core processor interface.
	*/

	override lateinit var inputStreams: MutableList<Stream>
	override lateinit var outputStream: Stream
	override lateinit var descriptor: ProcessorDescriptor
	override lateinit var id: UUID

	override fun stop() {
		println("Stopping")//To change body of created functions use File | Settings | File Templates.
	}

	override fun start() {
		println("Starting")
	}

	override fun process(): IgniteStream {
		var output = IgniteStream("[UUID]")
		println("Processing")
		return output
	}
}

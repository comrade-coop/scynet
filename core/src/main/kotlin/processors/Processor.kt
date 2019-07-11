package ai.scynet.core.processors
import ai.scynet.core.processors.Stream
import ai.scynet.core.descriptors.ProcessorDescriptor
import org.apache.ignite.IgniteDataStreamer
import org.apache.ignite.compute.ComputeTaskContinuousMapper
import java.io.Serializable
import java.util.*

interface Processor: Serializable {

	// TODO: DISCUSSION: Should this be tied with ignite?
	var id: UUID
	var inputStreams: List<Stream> //For now tied with ignite. TODO: Stream class and different Stream implementations (eg. with ignite)
	var outputStream: Stream // TODO: Research Compute task continuous mapper and research caches
	var descriptor: ProcessorDescriptor
	fun process(): ComputeTaskContinuousMapper // Should be seen as a producer to connect to a
}
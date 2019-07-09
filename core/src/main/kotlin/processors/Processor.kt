package ai.scynet.core.processors
import ai.scynet.core.descriptors.ProcessorDescriptor
import org.apache.ignite.compute.ComputeTaskContinuousMapper
import java.io.Serializable

interface Processor: Serializable {

	// TODO: DISCUSSION: Should this be tied with ignite?
	var id: String
	var engageCount: Int
	// var inputStreams: PRIVATE
	// var outputStream: PRIVATE
	var descriptor: ProcessorDescriptor
	fun process(): ComputeTaskContinuousMapper // Should be seen as a producer to connect to a
}
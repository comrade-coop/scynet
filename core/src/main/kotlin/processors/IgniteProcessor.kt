package ai.scynet.core.processors

import ai.scynet.core.descriptors.ProcessorDescriptor
import org.apache.ignite.compute.ComputeTaskContinuousMapper

class IgniteProcessor(override var id: String, override var engageCount: Int, override var descriptor: ProcessorDescriptor) : Processor {
	override fun process(): ComputeTaskContinuousMapper {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

}
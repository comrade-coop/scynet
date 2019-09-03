package ai.scynet.core.descriptors

import ai.scynet.core.processors.Processor
import java.util.*
import kotlin.reflect.KClass

data class ProcessorDescriptor(
	var id: UUID,
	var processorClass: KClass<out Processor>,
	var problem: String,
	var properties: Properties, // Should be used only in the custom class implementation (KClass<out Processor>,
	var inputStreams: List<StreamDescriptor>,
	var outputStream: StreamDescriptor
)
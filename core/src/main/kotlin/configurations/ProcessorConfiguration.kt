package ai.scynet.core.configurations

import ai.scynet.core.processors.Processor
import java.util.Properties
import kotlin.reflect.KClass

data class ProcessorConfiguration(
		var problem: String,
		var processorClass: KClass<out Processor>,
		var inputs: MutableList<String>,
		var properties: Properties
)

package ai.scynet.core.descriptors

import ai.scynet.core.processors.Processor
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.net.URI
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass


data class ProcessorDescriptor(
		var id: UUID,
		var processorClass: KClass<out Processor>,
		var problem: String,
		var properties: Properties, // Should be used only in the custom class implementation (KClass<out Processor>,
		var inputStreams: List<StreamDescriptor>,
		var outputStream: StreamDescriptor
)
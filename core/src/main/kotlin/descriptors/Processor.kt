package ai.scynet.core.descriptors

import ai.scynet.core.processors.Processor
import java.net.URI
import java.util.*


data class ProcessorDescriptor(var uri: URI) {
	var id: String = uri.query // UUID?
	var source: String = uri.query
	var proccessorClassName: String = uri.query // TODO: This should be an extended Processor -> Discussion
	var problem: String = uri.query
	var dependencies: ArrayList<StreamDescriptor>? = null
	var output: StreamDescriptor? = null

}
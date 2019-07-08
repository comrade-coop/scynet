package ai.scynet.core.descriptors

import ai.scynet.core.processors.Processor
import java.net.URI
import java.util.*

/*
	StreamDescriptor should contain all the needed information
	in order to create/instantiate a Processor via the ProcessorFactory
*/
data class StreamDescriptor (var uri: URI) {
	/*
		TODO: Should discuss a URI query standard and follow it. uri.query/fragment will be replaced
	 	TODO: Discuss the semantic type stuff and serialization (Protobuf)
	*/
	var id: String = uri.query // UUID?
	var source: String = uri.query
	var proccessorClassName: String = uri.query // TODO: This should be an extended Processor -> Discussion
	var problem: String = uri.query
	var dependencies: String = uri.fragment
}
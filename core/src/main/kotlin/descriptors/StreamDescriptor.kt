package ai.scynet.core.descriptors

import ai.scynet.core.processors.Processor
import java.net.URI
import java.util.*

// TODO: Move this to ai.scynet.common or leave in core after small refactoring

fun propFromUri(uri: URI): Properties {
	val properties = Properties()
	properties.load(uri.query.replace('&', '\n').byteInputStream())
	return properties
}


data class StreamDescriptor (var uri: URI) {
	/*
		Stream descriptor describes a certain stream, by source, problem, input, output dependencies
		Streams reside in the stream registry (IgniteRegistry("Stream")).
		They will most likely be queried by ID from there.

		URI STANDARD: stream://[owner_id]@[host_address]?config=data#[problem]

	 	TODO: Discuss the semantic type stuff and serialization (Protobuf)
	*/

	var id: UUID = UUID.nameUUIDFromBytes(uri.toString().toByteArray())// UUID?
	var properties: Properties = propFromUri(uri) // Uses uri.query to create a java.util.Properties class
	var ownerId: String = uri.authority
	var host_adress: String = uri.host
	var problem: String = uri.fragment
}

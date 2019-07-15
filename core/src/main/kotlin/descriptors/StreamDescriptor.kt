package ai.scynet.core.descriptors

import ai.scynet.common.propFromUri
import java.net.URI
import java.util.*

/*
	Stream descriptor describes a certain stream, by source, problem, input, output dependencies
	Streams reside in the stream registry (IgniteRegistry("Stream")).
	They will most likely be queried by ID from there.

	URI STANDARD: stream://[owner_id]@[host_address]?config=data#[problem]

	 TODO: Discuss the semantic type stuff and serialization (Protobuf)
*/
data class StreamDescriptor (
		var id: UUID,
		var properties: Properties,
		var ownerId: String,
		var hostAddress: String,
		var problem: String
	) {

	companion object {
		fun fromURI(uri: URI): StreamDescriptor {
			var id: UUID = UUID.nameUUIDFromBytes(uri.toString().toByteArray())// UUID?
			var properties: Properties = propFromUri(uri) // Uses uri.query to create a java.util.Properties class
			var ownerId: String = uri.authority
			var hostAddress: String = uri.path
			var problem: String = uri.fragment

			return StreamDescriptor(id, properties, ownerId, hostAddress, problem)
		}

		fun fromStringURI(stringURI: String): StreamDescriptor {
			return fromURI(URI.create(stringURI))
		}
	}

}

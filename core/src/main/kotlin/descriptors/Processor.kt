package ai.scynet.core.descriptors

import ai.scynet.core.processors.Processor
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.util.Properties
import java.net.URI
import java.nio.charset.StandardCharsets
import java.security.MessageDigest


// TODO: Move this to ai.scynet.common or leave in core after small refactoring
fun bytesToHex(hashInBytes: ByteArray): String {

	val sb = StringBuilder()
	for (b in hashInBytes) {
		sb.append(String.format("%02x", b))
	}
	return sb.toString()

}

data class ProcessorDescriptor(var uri: URI) {

	var id: String = bytesToHex(
			MessageDigest.getInstance("SHA-256").digest(
					uri.toString().toByteArray(StandardCharsets.UTF_8)
		) // idk hash of the URI - using as ID TODO: Discuss, since this is somewhat stupid
	)
	var ownerId: String = uri.authority
	var problem: String = uri.fragment

	/*
		Java Properties
		Contains a Processor extended class, and all kinds of different information
		in order to create a working processor
	*/
	var proccessorProperties: Properties = ObjectInputStream(
			uri.query.byteInputStream(StandardCharsets.UTF_8) // TODO: WARNING! Get the specific parameter, not working right now
	).readObject() as Properties

	init {
		// TODO: Define properties file, implement the property assigning here
		var dependencies: ArrayList<StreamDescriptor>? = null
		var output: StreamDescriptor? = null
		var processorClass: Processor? = null

	}


}
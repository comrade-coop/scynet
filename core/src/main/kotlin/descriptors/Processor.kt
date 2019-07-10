package ai.scynet.core.descriptors

import ai.scynet.core.processors.Processor
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.net.URI
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import kotlin.collections.ArrayList


// TODO: Move this to ai.scynet.common or leave in core after small refactoring
fun bytesToHex(hashInBytes: ByteArray): String {

	val sb = StringBuilder()
	for (b in hashInBytes) {
		sb.append(String.format("%02x", b))
	}
	return sb.toString()

}

fun fromUri(uri: URI): Properties {
	val properties = Properties()
	properties.load(uri.query.replace('&', '\n').byteInputStream())
	return properties
}

data class ProcessorDescriptor(var uri: URI) {

	var id: UUID = UUID.fromString(uri.toString())
	var ownerId: String = uri.authority
	var problem: String = uri.fragment

	/*
		Java Properties
		Contains a Processor extended class, and all kinds of different information
		in order to create a working processor
	*/
	var properties: Properties = fromUri(uri)

	init {
		// TODO: Define properties file, implement the property assigning here
		var dependencies: ArrayList<StreamDescriptor>? = null
		var output: StreamDescriptor? = null
		var processorClass: Processor? = null

	}




}
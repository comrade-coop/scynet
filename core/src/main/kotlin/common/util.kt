package ai.scynet.common

import java.net.URI
import java.util.*

fun propFromUri(uri: URI): Properties {
	val properties = Properties()
	properties.load(uri.query.replace('&', '\n').byteInputStream())
	return properties
}
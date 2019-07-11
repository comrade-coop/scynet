package ai.scynet.core.processors

import ai.scynet.core.descriptors.StreamDescriptor

abstract class Stream {
	lateinit var descriptor: StreamDescriptor

	abstract fun <K, V> listen(callback: (K, V) -> Unit)

	// TODO: Decide on a name (push, add, append)
	abstract fun <K, V> append(key: K, value: V)
}
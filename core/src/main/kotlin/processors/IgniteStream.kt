package ai.scynet.core.processors

import org.apache.ignite.Ignite
import org.koin.core.KoinComponent
import org.koin.core.inject

class IgniteStream(var name: String) : Stream(), KoinComponent {


	val ignite: Ignite by inject()
	val cache = ignite.getOrCreateCache<Any, Any>(name)
	val streamer = ignite.dataStreamer<Any, Any>(name)

	private var listener: (Any, Any) -> Unit = { _,_ -> }

	init {
		// TODO: Should be user configurable
		streamer.autoFlushFrequency(1)
		streamer.receiver { cache, updates ->
			updates.forEach {
				listener(it.key, it.value)
			}
		}
	}

	override fun <K, V> listen(callback: (K, V) -> Unit) {
		listener = callback as (Any, Any) -> Unit
	}

	override fun <K, V> append(key: K, value: V) {
		streamer.addData(key, value)
	}
}
package ai.scynet.core.processors

import ai.scynet.core.descriptors.StreamDescriptor
import org.apache.ignite.Ignite
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.net.URI
import java.util.*

class IgniteStream(name: String, hostAddress: String, problem: String, properties: Properties) : Stream(), KoinComponent {


	val ignite: Ignite by inject()
	val cache = ignite.getOrCreateCache<Any, Any>(name)
	val streamer = ignite.dataStreamer<Any, Any>(name)

	// TODO: Discuss this and the eventual removal of the URI and config
	var ownerId = "testOwner123" // This should be taken from the blockchain
	var	mockConfig = "test=123&data_type=tensor" // TEMPORARY TEST //TODO: REMOVE

	private var listener: (Any, Any) -> Unit = { _,_ -> }

	init {
		// TODO: Should be user configurable
		// URI STANDARD: stream://[owner_id]@[host_address]?config=data#[problem]
		descriptor = StreamDescriptor.fromStringURI("stream://$ownerId@$this.hostAddress?$mockConfig#$this.problem")

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
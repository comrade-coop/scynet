package ai.scynet.core.processors

import ai.scynet.core.descriptors.StreamDescriptor
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.query.ContinuousQuery
import org.apache.ignite.cache.query.Query
import org.apache.ignite.cache.query.QueryCursor
import org.apache.ignite.cache.query.ScanQuery
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.Closeable
import java.net.URI
import java.util.*
import javax.cache.event.CacheEntryEvent
import javax.cache.event.CacheEntryUpdatedListener

class IgniteStream(name: String, hostAddress: String, problem: String, properties: Properties) : Stream(), KoinComponent {


	val ignite: Ignite by inject()
	val cache = ignite.getOrCreateCache<Any, Any>(name)
	val streamer = ignite.dataStreamer<Any, Any>(name)

	// TODO: Discuss this and the eventual removal of the URI and config
	var ownerId = "testOwner123" // This should be taken from the blockchain
	var	mockConfig = "test=123&data_type=tensor" // TEMPORARY TEST //TODO: REMOVE


	init {
		// TODO: Should be user configurable
		// URI STANDARD: stream://[owner_id]@[host_address]?config=data#[problem]
		descriptor = StreamDescriptor.fromStringURI("stream://$ownerId@$this.hostAddress?$mockConfig#$this.problem")

		streamer.autoFlushFrequency()
	}

	override fun <K, V> listen(callback: (K, V, V?) -> Unit) : AutoCloseable {
		var query = ContinuousQuery<K, V>()


		// Because we cannot change the real localListener after the query is already performed we make a temporary one.
		var realListener: (Iterable<CacheEntryEvent<out K, out V>>) -> Unit = {

		}

		query.localListener = CacheEntryUpdatedListener {
			realListener(it)
		}

		query.initialQuery = ScanQuery<K,V>()
		var c: AutoCloseable = cache.query(query)

		var cursor = cache.query(query)

		realListener = {
			for (entry in it) {
				println(it)
				callback(entry.key, entry.value, entry.oldValue)
			}
		}

//		for (entry in cursor) {
//			callback(entry.key, entry.value, null)
//		}

		return cursor
	}

	override fun <K, V> append(key: K, value: V) {
		streamer.addData(key, value)
//		streamer.flush()
	}
}
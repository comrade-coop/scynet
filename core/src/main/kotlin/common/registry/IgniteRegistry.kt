package ai.scynet.core.common.registry

import ai.scynet.common.registry.Registry
import ai.scynet.core.common.registry.cursors.IgniteCursor
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.CacheEntryEventSerializableFilter
import org.apache.ignite.cache.query.ContinuousQuery
import org.apache.ignite.cache.query.ScanQuery
import org.koin.core.KoinComponent
import org.koin.core.inject
import javax.cache.event.CacheEntryUpdatedListener

open class IgniteRegistry<K, V>(var name: String) : Registry<K, V>, KoinComponent {
    val ignite: Ignite by inject()
    var cache: IgniteCache<K, V> = ignite.getOrCreateCache(name)

    override fun put(key: K, value: V) {
        cache.put(key, value)
    }

    override fun get(key: K): V? {
        return cache.get(key)
    }

    override fun delete(key: K) {
        cache.remove(key)
    }

    override fun query(predicate: (K, V) -> Boolean, callback: (K, V) -> Unit): IgniteCursor<K, V> {
        var query = ContinuousQuery<K, V>()
        query.localListener = CacheEntryUpdatedListener {
            it.forEach {
                callback(it.key, it.value)
            }
        }

        query.remoteFilter = CacheEntryEventSerializableFilter {
            predicate(it.key, it.value)
        }

        query.initialQuery = ScanQuery<K,V>(predicate)

        return IgniteCursor(cache.query(query))
    }
}
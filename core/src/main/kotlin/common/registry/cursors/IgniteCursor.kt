package ai.scynet.core.common.registry.cursors

import org.apache.ignite.cache.query.QueryCursor
import javax.cache.Cache

class IgniteCursor<K,V>(private val queryCursor: QueryCursor<Cache.Entry<K, V>>):Cursor<K,V> {
    override fun iterator(): QueryIterator<K, V> {
        return QueryIterator(queryCursor.iterator())
    }

    override fun close() {
        queryCursor.close()
    }


}

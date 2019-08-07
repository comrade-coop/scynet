package ai.scynet.protocol

import javax.cache.Cache

class QueryIterator<K,V>(private val queryCursorIterator:MutableIterator<Cache.Entry<K,V>>): Iterator<Pair<K,V>> {

    override fun hasNext(): Boolean {
       return queryCursorIterator.hasNext()
    }

    override fun next(): Pair<K, V> {
        val next = queryCursorIterator.next()
        return Pair(next.key, next.value)
    }
}
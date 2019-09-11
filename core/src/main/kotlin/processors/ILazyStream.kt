package processors

import descriptors.LazyStreamDescriptor
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.query.QueryCursor
import javax.cache.Cache

interface ILazyStream{
    val classId: String
    var descriptor: LazyStreamDescriptor?
    fun fillMissingStreamData(from: Long, to: Long)
    fun fillMissingStreamData(from: Long)
    fun refreshStreamData(from: Long, to: Long)
    fun refreshStreamData(from:Long)
    fun <K, V> listen(callback: (K,V,V?) -> Unit): QueryCursor<Cache.Entry<K,V>>
    fun <K, V> listen(predicate: (K,V) -> Boolean, callback: (K,V,V?) -> Unit): AutoCloseable

    fun getCachee(): IgniteCache<Any, Any>
    fun dispose()

}
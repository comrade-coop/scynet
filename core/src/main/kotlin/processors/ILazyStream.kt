package processors

import descriptors.LazyStreamDescriptor

interface ILazyStream{
    val classId: String
    var descriptor: LazyStreamDescriptor?
    fun fillMissingStreamData(from: Long, to: Long)
    fun fillMissingStreamData(from: Long)
    fun refreshStreamData(from: Long, to: Long)
    fun refreshStreamData(from:Long)
    fun <K, V> listen(callback: (K,V,V?) -> Unit): AutoCloseable
    fun <K, V> listen(predicate: (K,V) -> Boolean, callback: (K,V,V?) -> Unit): AutoCloseable

    fun dispose()

}
package ai.scynet.common.registry

interface Registry<K, V> {
    fun put(key: K, value: V)
    fun get(key: K): V?
    fun delete(key: K)
    fun query(predicate: (K, V) -> Boolean, callback: (K, V) -> Unit)
}
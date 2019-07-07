package registry

import ai.scynet.common.registry.Registry

interface EngageableRegistry<K, V>: Registry<K, V> {
    fun engage(key: K)

    fun disengage(key: K)
}
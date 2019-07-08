package ai.scynet.common.registry

import ai.scynet.common.LifeCycle
import java.io.Closeable

interface EngageableRegistry<K, V : LifeCycle>: Registry<K, V> {
    fun engage(key: K): Long

    fun disengage(key: K): Long
}
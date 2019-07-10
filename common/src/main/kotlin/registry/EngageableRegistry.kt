package ai.scynet.common.registry

import ai.scynet.common.LifeCycle
import java.io.Closeable

interface EngageableRegistry<K, V : LifeCycle>: Registry<K, V> {
    fun engage(key: K): Int

    fun disengage(key: K): Int
}
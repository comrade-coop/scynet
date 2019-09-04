package ai.scynet.core.common.registry.cursors

interface Cursor<K,V>: AutoCloseable, Iterable<Pair<K,V>> {
}
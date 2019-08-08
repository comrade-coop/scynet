package ai.scynet.protocol

interface Cursor<K,V>: AutoCloseable, Iterable<Pair<K,V>> {
}
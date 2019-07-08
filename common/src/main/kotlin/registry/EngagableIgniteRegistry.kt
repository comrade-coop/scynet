package ai.scynet.common.registry
import ai.scynet.common.*

class EngagableIgniteRegistry<K, V : LifeCycle>(name: String) : IgniteRegistry<K, V>(name), EngageableRegistry<K, V> {

	override fun engage(key: K): Long {
		var atomic = ignite.atomicLong("$name:${key.toString()}", 0, true)
		val resutl = atomic.incrementAndGet()
		if(resutl == 1L) {
			get(key)?.let {
				it.start()
			}
		}
		return resutl
	}

	override fun disengage(key: K): Long {
		var atomic = ignite.atomicLong("$name:${key.toString()}", 0, true)
		val result = atomic.decrementAndGet()
		if(result == 0L) {
			get(key)?.let {
				it.stop()
			}
		}
		return result;
	}

}
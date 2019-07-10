package ai.scynet.common.registry
import ai.scynet.common.*

class EngagableIgniteRegistry<K, V : LifeCycle>(name: String) : IgniteRegistry<K, V>(name), EngageableRegistry<K, V> {

	override fun engage(key: K): Int {
		var semaphore = ignite.semaphore("$name:${key.toString()}", Int.MAX_VALUE, true, true)
		semaphore.acquire()
		val result = Int.MAX_VALUE - semaphore.availablePermits()
		if(result == 1) {
			get(key)?.let {
				it.start()
			}
		}
		return result
	}

	override fun disengage(key: K): Int {
		var semaphore = ignite.semaphore("$name:${key.toString()}", Int.MAX_VALUE, true, true)
		semaphore.release()
		val result = Int.MAX_VALUE - semaphore.availablePermits()
		if(result == 0) {
			get(key)?.let {
				it.stop()
			}
		}
		return result
	}

}
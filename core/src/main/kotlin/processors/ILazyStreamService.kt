package processors

import org.apache.ignite.services.Service

interface ILazyStreamService<V>: Service{
    val engagementTimeoutSeconds: Int

    fun engageLiveStream()

    fun fillMissingStreamData(from: Long, to: Long)

    fun fillMissingStreamData(from: Long)

    fun refreshStreamData(from: Long, to: Long)

    fun refreshStreamData(from: Long)
}
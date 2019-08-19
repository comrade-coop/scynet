package processors

import org.apache.ignite.services.Service

interface IContinuousStreamService: Service{
    val engagementTimeoutSeconds: Int

    fun engageLiveStream() {
        //TODO: Self cancel() after engagementTimeoutSeconds since the last engage
    }

    fun fillMissingStreamData(from: Long, to: Long)

    fun fillMissingStreamData(from: Long)

    fun refreshStreamData(from: Long, to: Long)

    fun refreshStreamData(from: Long)

}
package ai.scynet.trainer.mock

import ai.scynet.protocol.*
import org.apache.ignite.services.ServiceContext
import org.deeplearning4j.datasets.iterator.FloatsDataSetIterator
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import processors.LazyStreamService
import java.io.File
import java.lang.IllegalStateException
import java.security.Timestamp
import java.time.Instant
import java.util.*

class MockService: LazyStreamService<Long, TrainingJob>() {

    override fun init(ctx: ServiceContext?) {
        super.init(ctx)
    }

    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)

        // TODO: Use: ClassLoader.getSystemResourceAsStream("")
        var mockModel = File("trainer/src/main/kotlin/mock/mockModel.json").inputStream().readBytes().toString(Charsets.UTF_8)

        // TODO: Finished Job Stream -> ExecutorStream (Parse the identifier) -> Execute

        var mockExecutorID = UUID.randomUUID() // Not real, just for authenticity

        var dataX = Nd4j.readNumpy("./trainer/src/main/kotlin/mock/xbnc_n.csv", ",");
        var dataY = Nd4j.readNumpy("./trainer/src/main/kotlin/mock/ybnc_n.csv", ",");

        var dataDictionary: HashMap<String, INDArray> = hashMapOf("x" to dataX, "y" to dataY)

        var numOfJobs = 5

        for (i in 0..numOfJobs) {
            var date = Date().time
            var job = TrainingJob(
                    UUID.randomUUID(),
                    mockExecutorID.toString(),
                    "trainerCluster",
                    "basic",
                    mockModel,
                    dataDictionary,
                    UNTRAINED()
            )
            cache.put(date, job)
        }
    }

    override fun cancel(ctx: ServiceContext?) {
        super.cancel(ctx)
    }

}

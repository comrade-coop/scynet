import ai.scynet.protocol.TRAINED
import org.apache.ignite.Ignite
import org.apache.ignite.lang.IgniteRunnable
import org.apache.logging.log4j.LogManager
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*

class IgniteEvaluatingJob(private val agentId: String): IgniteRunnable, KoinComponent {
    private val ignite: Ignite by inject()
    private val logger = LogManager.getLogger(this::class.qualifiedName)


    override fun run() {
        logger.warn("Initializing Evaluation on Latest Dataset...")

        val weightsFile = File("./trainer/src/main/kotlin/mock/temp/results/${agentId}_w.h5")
        val weightsCache = ignite.getOrCreateCache<String, ByteArray>("weights")
        val weights = weightsCache.get(agentId)
        weightsFile.writeBytes(weights)

        val pb = ProcessBuilder("bash", "./trainer/src/main/python/startEvaluation.sh",
                agentId,
                "dataset.csv"
        )

        pb.redirectErrorStream(true)
        val p = pb.start()
        val output  = BufferedReader(InputStreamReader(p.inputStream))

        for(out in output.lines()) {
            logger.trace("Python[OUT]: $out")

            if (out.split("=")[0] == "EVALUATION_DONE") {

                var perf = out.split("=")[1] // Parse the performance here
                val performanceCache = ignite.getOrCreateCache<String, Double>("tmp_perf")
                val actualPerformance: Double = if (perf.toDoubleOrNull() != null) perf.toDouble() else 0.0
                logger.debug("Class of actualPerformance -> ${actualPerformance::class.qualifiedName} --- actualPerformance = $actualPerformance")
                performanceCache.put(agentId, actualPerformance )
            }
        }
        p.waitFor()
        weightsFile.delete()
    }
}
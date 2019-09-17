package ai.scynet.trainer

import ai.scynet.protocol.TRAINED
import ai.scynet.protocol.TrainingJob
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.lang.IgniteRunnable
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.inject
import org.koin.dsl.module
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*

class IgnitePredictingJob: IgniteRunnable, KoinComponent {

    private lateinit var agentId: String
    private lateinit var agentEgg: String
    private lateinit var x: INDArray
    private lateinit var callback: (y: Double) -> Unit

    lateinit var trainingJob: TrainingJob;
    lateinit var addToFinishedJobsStream : (t: Long, tJob: TrainingJob) -> Unit;
    protected val ignite: Ignite by inject()

    override fun run() {
        initTrainer()
    }

    constructor(agentId: String, agentEgg: String, x: INDArray, func: (y: Double) -> Unit){
        this.agentId = agentId
        this.agentEgg = agentEgg
        this.x = x
        this.callback = func
    }

    private fun initTrainer(){
        println("WARNING: Initializing Predictor... <--------------------------------------#-------------#-------#----#---#--#-##")

        val xPath = "./trainer/src/main/kotlin/mock/temp/data/xTEMP${agentId}.npy"

        // TODO: We don't even need to use csv when passing stuff to the trainer, that's cool, but discuss
        Nd4j.writeAsNumpy(x, File(xPath))

        // The arguments are as follows --agentId -x
        val pb = ProcessBuilder("bash", "./trainer/src/main/python/startPrediction.sh", agentId, xPath)

        pb.redirectErrorStream(true)
        val p = pb.start()
        val output  = BufferedReader(InputStreamReader(p.inputStream))

        for(out in output.lines()) {
            println("Python[OUT]: ${out}")

            if (out.split("=")[0] == "PREDICTION_DONE") {

                var prediction = out.split("=")[1] // Parse the performance here
                callback(if (prediction.toDoubleOrNull() != null) prediction.toDouble() else -1.0)
            }
        }
    }
}
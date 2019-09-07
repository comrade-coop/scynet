package ai.scynet.trainer

import org.apache.ignite.lang.IgniteRunnable
import java.io.BufferedReader
import java.io.InputStreamReader

class IgniteTrainingJob: IgniteRunnable {
    override fun run() {
        initTrainer()
    }
    private fun initTrainer(){
        val pb = ProcessBuilder("bash", "trainer/src/main/python/startTrainer.sh")
        pb.redirectErrorStream(true)
        val p = pb.start()
        val output  = BufferedReader(InputStreamReader(p.inputStream))
        for(out in output.lines()){
            println("output -> ${out}")
        }
    }
}
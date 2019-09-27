package harvester

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object LoggerExample {

    private val logger = LogManager.getLogger(LoggerExample::class.java)

    @JvmStatic
    fun main(args: Array<String>) {

        for (i in 0..9999) {
            logger.info("Logger example...")
            try {
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }
}
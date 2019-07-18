import ai.scynet.core.annotations.Inputs
import ai.scynet.core.annotations.Output
import ai.scynet.core.annotations.Type
import ai.scynet.core.descriptors.ProcessorDescriptor
import ai.scynet.core.processors.IgniteStream
import ai.scynet.core.processors.Processor
import ai.scynet.core.processors.Stream
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.cache.CacheEntryProcessor
import org.apache.ignite.cluster.ClusterNode
import org.apache.ignite.compute.*
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.events.EventType
import org.apache.ignite.lang.IgniteFuture
import org.apache.ignite.resources.*
import org.apache.ignite.services.Service
import org.apache.ignite.services.ServiceContext

import org.koin.core.KoinComponent
import org.koin.core.inject
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicInteger
import javax.cache.processor.MutableEntry
import org.apache.ignite.compute.ComputeJobContext
import org.apache.ignite.lang.IgniteBiPredicate
import org.apache.ignite.lang.IgniteClosure
import org.apache.ignite.resources.JobContextResource



@ComputeTaskName("BasicConsumerTask")
@ComputeTaskMapAsync
class BasicConsumerTask : ComputeTaskAdapter<String, String>() {

	@IgniteInstanceResource
	lateinit var ignite: Ignite

	@TaskContinuousMapperResource
	lateinit var mapper: ComputeTaskContinuousMapper

	override fun map(subgrid: MutableList<ClusterNode>?, string: String?): MutableMap<out ComputeJob, ClusterNode> {
		return mutableMapOf(Pair(object : ComputeJob {
			@JobContextResource
			lateinit var jobCtx: ComputeJobContext
			var state = AtomicInteger(0)

			var listener = IgniteBiPredicate<UUID, String> { id, msg ->
				println("Listen")

				mapper.send(object : ComputeJob {
					@TaskSessionResource
					lateinit var session: ComputeTaskSession

					override fun cancel() {
						session.setAttribute("cancel", true)
					}

					override fun execute(): Any {
						println(msg)
						return (msg).split(" ").lastIndex
					}
				})
				true
			}

			override fun cancel() {
				println("Cancel")

				ignite.message().stopLocalListen("text", listener)
				jobCtx.callcc()
			}

			override fun execute(): Any? {
				if(state.getAndIncrement() == 0){
					println("jobCtx: $jobCtx")
					ignite.message().localListen("text", listener)
					return jobCtx.holdcc<Unit>()
				}

				println("Hello world")
				return 0
			}
		}, subgrid!![0]))
	}

	override fun reduce(results: MutableList<ComputeJobResult>?): String? {

		return results!!.map { it.getData() as Int }.reduce { a, b -> a + b }.toString()
	}
}

interface S {
	fun run(it: String)
}


fun main() {
	var cfg = IgniteConfiguration()
	cfg.setPeerClassLoadingEnabled(true)
	cfg.setIncludeEventTypes(*EventType.EVTS_ALL_MINUS_METRIC_UPDATE)
	cfg.igniteInstanceName = "NAME"

	var ignite = Ignition.start(cfg)
	ignite.cluster().active()

	ignite.compute().localDeployTask(BasicConsumerTask::class.java, BasicConsumerTask::class.java.classLoader)

	var future = ignite.compute().executeAsync<String, String>("BasicConsumerTask", "random")


	while(true) {
		var line = readLine()
		if (line == "exit") {
			future.cancel()

		} else {
			ignite.message().send("text", line)

		}
	}
}
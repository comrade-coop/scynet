package ai.scynet.core.processors

import ai.scynet.core.annotations.Inputs
import ai.scynet.core.annotations.Output
import ai.scynet.core.annotations.Type
import ai.scynet.core.descriptors.ProcessorDescriptor
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cluster.ClusterNode
import org.apache.ignite.compute.*
import org.apache.ignite.lang.IgniteFuture
import org.apache.ignite.resources.IgniteInstanceResource
import org.apache.ignite.resources.JobContextResource
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import org.apache.ignite.resources.TaskContinuousMapperResource
import org.apache.ignite.resources.TaskSessionResource
import java.util.concurrent.atomic.AtomicInteger


@ComputeTaskName("BasicConsumerTask")
@ComputeTaskMapAsync
class BasicConsumerTask : ComputeTaskAdapter<IgniteStream, Nothing>() {

	@IgniteInstanceResource
	lateinit var ignite: Ignite

	@TaskContinuousMapperResource
	lateinit var mapper: ComputeTaskContinuousMapper

	override fun map(subgrid: MutableList<ClusterNode>?, stream: IgniteStream?): MutableMap<out ComputeJob, ClusterNode> {
		if(stream == null) return mutableMapOf()
		return mutableMapOf(Pair(object : ComputeJob {
			@JobContextResource
			lateinit var jobCtx: ComputeJobContext
			var state = AtomicInteger(0)

			override fun cancel() {
				jobCtx.callcc()
			}

			override fun execute(): Any? {
				if(state.getAndIncrement() == 0){
					println("jobCtx: $jobCtx")

					ClassLoader.getSystemClassLoader().getResourceAsStream("lorem.txt").bufferedReader().readLines().forEachIndexed { index, line ->
						mapper.send(object : ComputeJobAdapter() {
							override fun execute(): Any {
								return stream.append(index.toString(), line)
							}
						})
					}

					return jobCtx.holdcc<Unit>()
				}

				println("All data uploaded")
				return 0
			}
		}, subgrid!![0]))
	}

	override fun reduce(results: MutableList<ComputeJobResult>?): Nothing? {
		throw Exception("We shouldn't get into the reducer")
	}
}


@Inputs([])
@Output(Type("String"))
class BasicConsumerProcessor : Processor, KoinComponent {

	override lateinit var inputStreams: MutableList<Stream>
	override lateinit var descriptor: ProcessorDescriptor
	override lateinit var id: UUID

	override var outputStream: Stream = IgniteStream("guz", "localhost", "wordcount", Properties())


	val ignite: Ignite by inject()

	private var future: IgniteFuture<String>? = null

	init {
		ignite.compute().localDeployTask(BasicConsumerTask::class.java, BasicConsumerTask::class.java.classLoader)

	}

	override fun start() {
		future = ignite.compute().executeAsync<Stream, String>("BasicConsumerTask", outputStream)
	}

	override fun stop() {
		future?.let {
			it.cancel()
			return
		}
		println("An already not running processor should not stop.") // TODO: Use real logging
	}
}
package ai.scynet.core.processors


import ai.scynet.core.annotations.Inputs
import ai.scynet.core.annotations.Output
import ai.scynet.core.annotations.Type
import ai.scynet.core.descriptors.ProcessorDescriptor
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.cluster.ClusterNode
import org.apache.ignite.compute.*
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.events.EventType
import org.apache.ignite.lang.IgniteBiPredicate
import org.apache.ignite.resources.IgniteInstanceResource
import org.apache.ignite.resources.JobContextResource
import org.apache.ignite.resources.TaskContinuousMapperResource
import org.apache.ignite.resources.TaskSessionResource
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


@ComputeTaskName("BasicConsumerTask")
@ComputeTaskMapAsync
class BasicProcessorTask : ComputeTaskAdapter<String, String>() {

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

@Inputs([Type("Int"), Type("String"), Type("String")])
@Output(Type("Tensor"))
class BasicProcessor: Processor {
	/*
		A very basic processor, which implements the core processor interface.
	*/

	override lateinit var inputStreams: MutableList<Stream>
	override lateinit var outputStream: Stream
	override lateinit var descriptor: ProcessorDescriptor
	override lateinit var id: UUID

	init {
		id = UUID.randomUUID()
		outputStream = IgniteStream("$id", "localhost:3343", "StockPricePrediction", Properties()) //TODO: Remove hardcoding stuff
	}

	override fun stop() {
		println("Stopping")//To change body of created functions use File | Settings | File Templates.
	}

	override fun start() {
		val stream = inputStreams.get(0)
		stream.listen { key: String, value: String, old: String? ->
			println("$key: $old -> $value")
		}
	}

	init {
		//var output = IgniteStream("[UUID]",)
		println("Processing")
		//TODO("Implement")
	}
}

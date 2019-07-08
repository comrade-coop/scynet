import ai.scynet.common.LifeCycle

data class TestClass(val id: Int): LifeCycle {
	override fun start() {
		println("Starting: $id")

	}

	override fun stop() {
		println("Dead: $id")
	}
}
processors {
    processor {
        problem = "no problem"
        processorClass = BasicProcessor::class
        inputs = mutableListOf("hello", "there", "is", "no", "problem")
        properties = Properties()
    }
    processor {
        problem = "huge problem"
        processorClass = BasicProcessor::class
        inputs = mutableListOf("now", "we", "have", "a", "huge", "problem")
        properties = Properties()
    }
}
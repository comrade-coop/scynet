processors {
    processor {
        problem = "WordCount"
        processorClass = BasicConsumerProcessor::class
        inputs = mutableListOf()
        properties = Properties()
    }
    processor {
        problem = "WordCount"
        processorClass = BasicProcessor::class
        inputs = mutableListOf("WordCount")
        properties = Properties()
    }
}
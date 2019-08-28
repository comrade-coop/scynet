package descriptors

class Properties {
    private val properties: HashMap<String, Any> = HashMap()
    fun put(key: String, value: Any){
        properties.put(key, value)
    }
    fun get(key:String): Any?{
        if(properties.containsKey(key))
            return properties.get(key)
        return null
    }
}
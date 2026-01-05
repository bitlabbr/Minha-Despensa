package com.bill.minhadispensa.minhadispensa

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello world from, ${platform.name}!"
    }
}
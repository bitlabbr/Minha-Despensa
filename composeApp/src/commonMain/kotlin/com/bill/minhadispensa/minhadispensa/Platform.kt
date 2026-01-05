package com.bill.minhadispensa.minhadispensa

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
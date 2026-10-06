package com.tb.chess

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
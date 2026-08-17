package com.kuzeykapisi.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
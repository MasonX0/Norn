package ru.bpo.norn

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
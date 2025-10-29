package ru.bpo.norn.commonMain.ui

import ru.bpo.norn.getPlatform

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}
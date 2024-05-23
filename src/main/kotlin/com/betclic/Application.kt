package com.betclic

import com.betclic.plugins.configureKoin
import com.betclic.plugins.configureRouting
import com.betclic.plugins.configureSerialization
import com.betclic.services.IPlayerService
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.koin.ktor.ext.inject

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    configureKoin()

    configureSerialization()

    val playerService: IPlayerService by inject()
    configureRouting(playerService)
}

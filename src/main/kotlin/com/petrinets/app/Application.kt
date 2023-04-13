package com.petrinets.app

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

import com.petrinets.app.config.configureRouting
import com.petrinets.app.config.configureSerialization
import com.petrinets.app.config.configureCors

fun main() {
    embeddedServer(
        Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module,
        watchPaths = listOf("classes")
    ).start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureRouting()
    configureCors()
}

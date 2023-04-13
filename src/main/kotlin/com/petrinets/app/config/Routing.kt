package com.petrinets.app.config

import com.petrinets.contexts.engine.infrastructure.rest.engineRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

import com.petrinets.contexts.engine.infrastructure.rest.postCalculateTransitionController

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond(mapOf("server" to "ok", "message" to "running"))
        }

        route("/api") {
            engineRoutes()
        }
    }
}

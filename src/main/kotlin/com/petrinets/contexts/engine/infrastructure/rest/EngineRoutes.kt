package com.petrinets.contexts.engine.infrastructure.rest

import io.ktor.server.routing.*

fun Route.engineRoutes() {
    route("/v1/petrinets/engine") {
        postCalculateTransitionController()
    }
}
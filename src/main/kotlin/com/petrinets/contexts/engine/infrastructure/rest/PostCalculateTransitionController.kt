package com.petrinets.contexts.engine.infrastructure.rest

import buildReachabilityTree
import com.petrinets.contexts.engine.domain.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.kotlinx.multik.ndarray.operations.toList

import parseReachabilityTreeToJSON


fun Route.postCalculateTransitionController() {
    route("/transitions/fire") {
        post {
            try {
                val firePetriNetSerializer = call.receive<FirePetriNetSerializer>()

                val petriNet = PetriNet(
                    firePetriNetSerializer.petriNet.inputs.map { i -> Input(i.place, i.transition, i.value) },
                    firePetriNetSerializer.petriNet.outputs.map { o -> Output(o.place, o.transition, o.value) },
                    firePetriNetSerializer.petriNet.transitions.map { t -> Transition(t.name) },
                    firePetriNetSerializer.petriNet.places.map { p -> Place(p.name, p.tokens) },
                )

                val newMarkings = petriNet.fireTransitions(
                    firePetriNetSerializer.transitionsToFire.map { t -> Transition(t.name) }
                )

                call.respond(mapOf("markings" to newMarkings.toList()))
            } catch (e: Throwable) {
                call.respond(mapOf("errors" to e.message))
            }
        }
    }

    route("/transitions/check-enables") {
        post {
            try {
                val firePetriNetSerializer = call.receive<FirePetriNetSerializer>()

                val petriNet = PetriNet(
                    firePetriNetSerializer.petriNet.inputs.map { i -> Input(i.place, i.transition, i.value) },
                    firePetriNetSerializer.petriNet.outputs.map { o -> Output(o.place, o.transition, o.value) },
                    firePetriNetSerializer.petriNet.transitions.map { t -> Transition(t.name) },
                    firePetriNetSerializer.petriNet.places.map { p -> Place(p.name, p.tokens) },
                )

                val isEnabled = petriNet.transitionsIsEnable(
                    firePetriNetSerializer.transitionsToFire.map { t -> Transition(t.name) }
                )

                call.respond(mapOf("isEnabled" to isEnabled))
            } catch (e: Throwable) {
                call.respond(mapOf("errors" to e.message))
            }
        }
    }

    route("/reachability-tree") {
        post {
            try {
                val petriNetSerializer = call.receive<PetriNetSerializer>()

                val petriNet = PetriNet(
                    petriNetSerializer.inputs.map { i -> Input(i.place, i.transition, i.value) },
                    petriNetSerializer.outputs.map { o -> Output(o.place, o.transition, o.value) },
                    petriNetSerializer.transitions.map { t -> Transition(t.name) },
                    petriNetSerializer.places.map { p -> Place(p.name, p.tokens) },
                )

                val tree = buildReachabilityTree(petriNet.markings, petriNet)
                val treeJSON = parseReachabilityTreeToJSON(tree)
                call.respondText(treeJSON, ContentType.Application.Json)
            } catch (e: Throwable) {
                call.respond(mapOf("errors" to e.message))
            }
        }
    }
}

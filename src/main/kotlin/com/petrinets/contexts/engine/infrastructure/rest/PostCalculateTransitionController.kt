package com.petrinets.contexts.engine.infrastructure.rest

import com.petrinets.contexts.engine.domain.PetriNet
import com.petrinets.contexts.engine.domain.buildReachabilityTree
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.kotlinx.multik.ndarray.operations.toList

@Serializable
data class Place(val name: String, val tokens: Int)

@Serializable
data class Transition(val name: String)

@Serializable
data class Input(val place: String, val transition: String, val value: Int)

@Serializable
data class Output(val transition: String, val place: String, val value: Int)

@Serializable
data class PetriNetSerializer(
    val inputs: Array<Input>,
    val outputs: Array<Output>,
    val transitions: Array<Transition>,
    val places: Array<Place>,
)

@Serializable
data class FirePetriNetSerializer(
    val petriNet: PetriNetSerializer,
    val transitionsToFire: Array<Transition>
)

fun Route.postCalculateTransitionController() {
    route("/transitions/fire") {
        post {
            val firePetriNetSerializer = call.receive<FirePetriNetSerializer>()

            val petriNet = PetriNet(
                firePetriNetSerializer.petriNet.inputs,
                firePetriNetSerializer.petriNet.outputs,
                firePetriNetSerializer.petriNet.transitions,
                firePetriNetSerializer.petriNet.places,
            )

            val newMarkings = petriNet.fireTransitions(firePetriNetSerializer.transitionsToFire)

            call.respond(mapOf("markings" to newMarkings.toList()))
        }
    }

    route("/transitions/check-enables") {
        post {
            val firePetriNetSerializer = call.receive<FirePetriNetSerializer>()

            val petriNet = PetriNet(
                firePetriNetSerializer.petriNet.inputs,
                firePetriNetSerializer.petriNet.outputs,
                firePetriNetSerializer.petriNet.transitions,
                firePetriNetSerializer.petriNet.places,
            )

            val isEnabled = petriNet.transitionsIsEnable(firePetriNetSerializer.transitionsToFire)

            call.respond(mapOf("isEnabled" to isEnabled))
        }
    }

    route("/reachability-tree") {
        post {
            val firePetriNetSerializer = call.receive<PetriNetSerializer>()

            val petriNet = PetriNet(
                firePetriNetSerializer.inputs,
                firePetriNetSerializer.outputs,
                firePetriNetSerializer.transitions,
                firePetriNetSerializer.places,
            )

            val tree = buildReachabilityTree(petriNet.markings, petriNet)

            call.respond(mapOf("tree" to tree.toString()))
        }
    }
}

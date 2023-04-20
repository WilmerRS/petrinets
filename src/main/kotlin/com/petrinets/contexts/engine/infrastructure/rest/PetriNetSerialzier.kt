package com.petrinets.contexts.engine.infrastructure.rest

import kotlinx.serialization.Serializable


@Serializable
data class PlaceSerializer(val name: String, val tokens: Int)

@Serializable
data class TransitionSerializer(val name: String)

@Serializable
data class InputSerializer(val place: String, val transition: String, val value: Int)

@Serializable
data class OutputSerializer(val transition: String, val place: String, val value: Int)

@Serializable
data class PetriNetSerializer(
    val inputs: List<InputSerializer>,
    val outputs: List<OutputSerializer>,
    val transitions: List<TransitionSerializer>,
    val places: List<PlaceSerializer>,
)

@Serializable
data class FirePetriNetSerializer(
    val petriNet: PetriNetSerializer,
    val transitionsToFire: List<TransitionSerializer>
)
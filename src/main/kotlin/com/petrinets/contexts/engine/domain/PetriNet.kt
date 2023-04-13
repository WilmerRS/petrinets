package com.petrinets.contexts.engine.domain

import com.petrinets.contexts.engine.infrastructure.rest.Input
import com.petrinets.contexts.engine.infrastructure.rest.Output
import com.petrinets.contexts.engine.infrastructure.rest.Place
import com.petrinets.contexts.engine.infrastructure.rest.Transition
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.D1Array
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import org.jetbrains.kotlinx.multik.ndarray.operations.minus
import org.jetbrains.kotlinx.multik.ndarray.operations.plus

data class Place(val name: String, val tokens: Int)

data class Transition(val name: String)

data class Input(val place: String, val transition: String, val value: Int)

data class Output(val transition: String, val place: String, val value: Int)

class PetriNet(
    val inputs: Array<Input>,
    val outputs: Array<Output>,
    val transitions: Array<Transition>,
    val places: Array<Place>,
) {
    private var inputMatrix = mk.zeros<Int>(0, 0)
    private var outputMatrix = mk.zeros<Int>(0, 0)
    var markings = mk.zeros<Int>(0)

    private val transitionsHashMap = HashMap<String, Int>()

    init {
        this.generateInputMatrix()
        this.generateOutputMatrix()
        this.generateMarkingsMatrix()
        this.generateTransitionsHashMap()
    }

    fun fireTransitions(transitions: Array<Transition>): D1Array<Int> {
        val transitionsIsEnable = this.transitionsIsEnable(transitions)
        if (!transitionsIsEnable) {
            throw Throwable("Error")
        }

        var inputTransitions = mk.zeros<Int>(this.places.size)
        transitions.withIndex().forEach { transition ->
            val rowInInputMatrixOfTransitionIndex =
                this.transitionsHashMap[transition.value.name] ?: throw Throwable("Transition not found")
            val rowInInputMatrixOfTransition = this.inputMatrix[rowInInputMatrixOfTransitionIndex]
            inputTransitions = inputTransitions.plus(rowInInputMatrixOfTransition)
        }

        var outputTransitions = mk.zeros<Int>(this.places.size)
        transitions.withIndex().forEach { transition ->
            val rowInOutputMatrixOfTransitionIndex =
                this.transitionsHashMap[transition.value.name] ?: throw Throwable("Transition not found")
            val rowInOutputMatrixOfTransition = this.outputMatrix[rowInOutputMatrixOfTransitionIndex]
            outputTransitions = outputTransitions.plus(rowInOutputMatrixOfTransition)
        }

        return this.markings.minus(inputTransitions).plus(outputTransitions)
    }

    fun transitionsIsEnable(transitions: Array<Transition>): Boolean {
        var transitionMarkings = mk.zeros<Int>(this.places.size)
        transitions.withIndex().forEach { transition ->
            val rowInInputMatrixOfTransitionIndex = this.transitionsHashMap[transition.value.name] ?: return false
            val rowInInputMatrixOfTransition = this.inputMatrix[rowInInputMatrixOfTransitionIndex]
            transitionMarkings = transitionMarkings.plus(rowInInputMatrixOfTransition)
        }

        val result = this.markings.minus(transitionMarkings)
        return mk.math.min(result) >= 0
    }

    private fun generateInputMatrix() {
        this.inputMatrix = mk.zeros(transitions.size, places.size)
        this.transitions.withIndex().forEach { transition ->
            this.places.withIndex().forEach { place ->
                val inputRelation = inputs.find { input ->
                    input.place == place.value.name && input.transition == transition.value.name
                }
                if (inputRelation != null) {
                    this.inputMatrix[transition.index, place.index] = inputRelation.value
                }
            }
        }
    }

    private fun generateOutputMatrix() {
        this.outputMatrix = mk.zeros(transitions.size, places.size)
        this.transitions.withIndex().forEach { transition ->
            this.places.withIndex().forEach { place ->
                val outputRelation = this.outputs.find { output ->
                    output.place == place.value.name && output.transition == transition.value.name
                }

                if (outputRelation != null) {
                    this.outputMatrix[transition.index, place.index] = outputRelation.value
                }
            }
        }
    }

    private fun generateMarkingsMatrix() {
        this.markings = mk.zeros<Int>(this.places.size)
        this.places.withIndex().forEach { place ->
            this.markings[place.index] = place.value.tokens
        }
    }

    private fun generateTransitionsHashMap() {
        this.transitions.withIndex().forEach { transition ->
            this.transitionsHashMap[transition.value.name] = transition.index
        }
    }
}

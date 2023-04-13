package com.petrinets.contexts.engine.domain

import org.jetbrains.kotlinx.multik.ndarray.data.D1Array
import java.util.LinkedList

class ReachabilityNode(
    val markings: D1Array<Int>,
    val children: MutableList<ReachabilityNode> = mutableListOf()
) {
    fun addChild(child: ReachabilityNode) {
        children.add(child)
    }
}

class ReachabilityTree(val root: ReachabilityNode) {
    fun addNode(parentMarkings: D1Array<Int>, childMarkings: D1Array<Int>) {
        val parentNode = findNode(parentMarkings, root)
        val childNode = ReachabilityNode(childMarkings)
        parentNode.addChild(childNode)
    }

    private fun findNode(markings: D1Array<Int>, currentNode: ReachabilityNode): ReachabilityNode {
        if (currentNode.markings == markings) {
            return currentNode
        }

        for (child in currentNode.children) {
            return findNode(markings, child)
        }

        throw NoSuchElementException("Node with markings $markings not found")
    }
}

fun buildReachabilityTree(initialMarking: D1Array<Int>, petriNet: PetriNet): ReachabilityTree {
    val rootNode = ReachabilityNode(initialMarking)
    val tree = ReachabilityTree(rootNode)
    val queue = LinkedList<ReachabilityNode>()

    queue.add(rootNode)

    while (queue.isNotEmpty()) {
        val node = queue.poll()

        petriNet.transitions.forEach { transition ->
            if (petriNet.transitionsIsEnable(arrayOf(transition))) {
                val newMarking = petriNet.fireTransitions(arrayOf(transition))
                val newNode = ReachabilityNode(newMarking)
                node.addChild(newNode)
                queue.add(newNode)
            }
        }
    }

    return tree
}

import com.petrinets.contexts.engine.domain.PetriNet
import org.jetbrains.kotlinx.multik.ndarray.data.D1Array
import org.jetbrains.kotlinx.multik.ndarray.operations.toList
import java.util.LinkedList

class ReachabilityNode(
    val markings: D1Array<Int>,
    val formattedMarkings: List<String>,
    val children: MutableList<ReachabilityNode> = mutableListOf()
) {
    fun addChild(child: ReachabilityNode) {
        children.add(child)
    }
}

class ReachabilityTree(val root: ReachabilityNode)

fun buildReachabilityTree(initialMarking: D1Array<Int>, petriNet: PetriNet): ReachabilityTree {
    val rootNode = ReachabilityNode(initialMarking.copy(), initialMarking.toList().map { m -> m.toString() })
    val reachabilityTree = ReachabilityTree(rootNode)
    val availableNodesList = LinkedList<ReachabilityNode>()
    val alreadyRegisteredMarkings = mutableSetOf<List<String>>()

    availableNodesList.add(rootNode)

    while (availableNodesList.isNotEmpty()) {
        val currentNode = availableNodesList.poll()
        petriNet.markings = currentNode.markings.copy()

        petriNet.transitions.forEach { transition ->
            val transitionsToFire = listOf(transition)
            val transitionsAreEnabled = petriNet.transitionsIsEnable(transitionsToFire)
            if (!transitionsAreEnabled) {
                return@forEach
            }

            val markingsAfterToFire = petriNet.fireTransitions(transitionsToFire)
            val firedFormatMarkings = formatMarkings(markingsAfterToFire, currentNode.formattedMarkings)

            val nodeIsDuplicated = firedFormatMarkings in alreadyRegisteredMarkings
            if (nodeIsDuplicated) {
                return@forEach
            }

            val nodeCreated = ReachabilityNode(markingsAfterToFire, firedFormatMarkings)

            availableNodesList.add(nodeCreated)
            alreadyRegisteredMarkings.add(firedFormatMarkings)
            currentNode.addChild(nodeCreated)
        }
    }

    return reachabilityTree
}

private fun formatMarkings(markingsAfterToFire: D1Array<Int>, currentMarkings: List<String>): MutableList<String> {
    val defaultGeneralMarking = "w"
    val firedFormatMarkings = mutableListOf<String>()
    markingsAfterToFire.toList().withIndex().forEach { marking ->
        if (currentMarkings[marking.index] == defaultGeneralMarking) {
            firedFormatMarkings.add(defaultGeneralMarking)
            return@forEach
        }
        firedFormatMarkings.add(marking.value.toString())
    }
    return firedFormatMarkings
}

private fun buildJson(node: ReachabilityNode, sb: StringBuilder, depth: Int) {
    appendTabs(sb, depth)
    sb.append("{\n")
    appendTabs(sb, depth + 1)
    if (node.children.isEmpty()) {
        sb.append("\"markings\": ${node.markings}\n")
    } else {
        sb.append("\"markings\": ${node.markings},\n")
        appendTabs(sb, depth + 1)
        sb.append("\"children\": [\n")
        for (i in 0 until node.children.size) {
            buildJson(node.children[i], sb, depth + 2)
            if (i < node.children.size - 1) {
                sb.append(",")
            }
            sb.append("\n")
        }
        appendTabs(sb, depth + 1)
        sb.append("]\n")
    }
    appendTabs(sb, depth)
    sb.append("}")
}

private fun appendTabs(sb: StringBuilder, depth: Int) {
    for (i in 0 until depth) {
        sb.append("\t")
    }
}

fun parseReachabilityTreeToJSON(tree: ReachabilityTree): String {
    val sb = StringBuilder()
    buildJson(tree.root, sb, 0)
    return sb.toString()
}
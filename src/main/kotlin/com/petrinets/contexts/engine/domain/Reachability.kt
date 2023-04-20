import com.petrinets.contexts.engine.domain.PetriNet
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

class ReachabilityTree(val root: ReachabilityNode)

fun buildReachabilityTree(initialMarking: D1Array<Int>, petriNet: PetriNet): ReachabilityTree {
    val rootNode = ReachabilityNode(initialMarking.copy())
    val tree = ReachabilityTree(rootNode)
    val queue = LinkedList<ReachabilityNode>()
    val visited = mutableSetOf<D1Array<Int>>()

    queue.add(rootNode)

    while (queue.isNotEmpty()) {
        val node = queue.poll()

        petriNet.markings = node.markings.copy()

        petriNet.transitions.forEach { transition ->
            if (petriNet.transitionsIsEnable(listOf(transition))) {
                val newMarking = petriNet.fireTransitions(listOf(transition))
                if (newMarking !in visited) {
                    val newNode = ReachabilityNode(newMarking)
                    petriNet.markings = newMarking.copy()
                    visited.add(newMarking)
                    node.addChild(newNode)
                    queue.add(newNode)
                }
            }
        }
    }

    return tree
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
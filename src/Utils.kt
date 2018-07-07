import java.io.File
import java.util.*
import kotlin.collections.ArrayList

object Utils {

    fun readFile(filepath: String): ArrayList<String> {
        val file = File(filepath)
        var content = ArrayList<String>()

        if (file.exists() && file.canRead()) {
            file.useLines {
                it.forEach { line ->
                    if (!line.isEmpty()) {
                        content.add(line)
                    }
                }
            }
        }
        return content
    }

    fun createAutomata(fileContent: ArrayList<String>): Automata {
        var tokens = ArrayList<String>()
        var expressions = ArrayList<String>()

        fileContent.forEach { line ->
            if (line.contains(Constants.ATTRIBUTION)) {
                expressions.add(line.replace(" ",""))
            } else {
                tokens.add(line.replace(" ",""))
            }
        }

        var automata = Automata()
        automata.parseTokens(tokens)
        automata.parseExpression(expressions)

        return automata
    }

    fun isStateAlive(searchState: State): Boolean {
        var queuedStates: Queue<State> = LinkedList()
        var reachedStates: ArrayList<State> = ArrayList()

        queuedStates.add(searchState)

        while (queuedStates.count() > 0) {
            var state = queuedStates.remove()

            state.getTransitions().forEach { transition ->
                if (!reachedStates.contains(transition.state)) {
                    if (transition.state.final) {
                        return true
                    }
                    queuedStates.add(transition.state)
                    reachedStates.add(transition.state)
                }
            }
        }
        return false
    }

}
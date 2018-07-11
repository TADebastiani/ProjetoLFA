import java.util.*
import kotlin.collections.ArrayList

class Automata {
    private val _alphabet: ArrayList<Char> = ArrayList()
    private val _states: ArrayList<State> = ArrayList()

//  Alphabet methods

    fun addSymbol(symbol: Char) {
        if (!_alphabet.contains(symbol)) {
            _alphabet.add(symbol)
            _alphabet.sort()
        }
    }

//    State method

    fun newState(): State {
        val state = State(_states.count().toString())
        _states.add(state)
        if (state.name == "0") state.initial = true
        return state
    }

    fun hasStates(): Boolean {
        return _states.count() > 0
    }

    fun getState(name: String): State {
        return _states.find { it.name == name }!!
    }

//    Parse methods

    fun parseExpression(expressions: ArrayList<String>) {
        var statesMap = mutableMapOf<String,String>()
        var currentState: State


        expressions.forEach { expression ->
            val splicedExpression = expression.split(Constants.ATTRIBUTION)
            var stateLabel = getStateLabel(splicedExpression[0])

            if (!hasStates()) newState()

            if (stateLabel == "S") {
                statesMap = mutableMapOf()
                statesMap[stateLabel] = "0"
            }

            if (statesMap.containsKey(stateLabel)) {
                currentState = getState(statesMap[stateLabel]!!)
            } else {
                currentState = newState()
                statesMap[stateLabel] = currentState.name
            }

            val rawTransitions = splicedExpression[1].split(Constants.SEPARATOR)
            rawTransitions.forEach {
                val destinationState: State
                val symbol: Char

                if (it.startsWith('<')) {
                    stateLabel = getStateLabel(it)
                    if (stateLabel.isEmpty()){
                        currentState.final = true
                    } else {
                        symbol = Constants.EPSILON
                        if (statesMap.containsKey(stateLabel)) {
                            destinationState = getState(statesMap[stateLabel]!!)
                        } else {
                            destinationState = newState()
                            statesMap[stateLabel] = destinationState.name
                        }
                        currentState.newTransition(symbol,destinationState)
                        addSymbol(symbol)
                    }
                } else {
                    symbol = it[0]
                    stateLabel = getStateLabel(it.substring(1))
                    if (statesMap.containsKey(stateLabel)) {
                        destinationState = getState(statesMap[stateLabel]!!)
                    } else {
                        destinationState = newState()
                        statesMap[stateLabel] = destinationState.name
                    }
                    currentState.newTransition(symbol,destinationState)
                    addSymbol(symbol)
                }
            }
        }
    }

    fun parseTokens(tokens: ArrayList<String>) {
        tokens.forEach { token ->
            var currentState = if (!hasStates()) newState() else getState("0")

            token.forEach { symbol ->
                val nextState = newState()
                currentState.newTransition(symbol, nextState)
                addSymbol(symbol)
                currentState = nextState
            }
            currentState.final = true
        }
    }

    private fun getStateLabel(rawState: String):String {
        return rawState.replace("<","").replace(">","").trim()
    }

    fun determinize() {
        val alphabet = _alphabet.toList()
        var hasChange: Boolean
        do {
            hasChange = false
            val states = _states.toList()
            states.forEach { currentState ->
                if (currentState.hasTransitions()) {
                    alphabet.forEach { symbol ->
                        val currentTransitions = currentState.getTransitions(symbol)
                        if (currentTransitions.isNotEmpty() && currentTransitions.count() > 1) {
                            hasChange = true
                            val joinedTransitions = ArrayList<Transition>()
                            val joinedState = newState()
                            currentTransitions.forEach { t ->
                                joinedTransitions.addAll(t.state.getTransitions())
                                currentState.removeTransition(t)
                            }
                            joinedState.addTransitions(joinedTransitions)
                            joinedState.final = joinedState.final || currentState.final
                            currentState.newTransition(symbol, joinedState)
                        }
                    }
                }
            }
        } while(hasChange)
    }

    fun removeDeads() {
        val deadStates: ArrayList<State> = ArrayList()

        _states.forEach { state ->
            if (!state.final) {
                if (!Utils.isStateAlive(state)) {
                    deadStates.add(state)
                }
            }
        }

        _states.removeIf { t -> deadStates.contains(t)}

        _states.forEach { state ->
            val newTransitions = state.getTransitions().filter {t ->
                _states.contains(t.state)
            } as ArrayList<Transition>

            state.removeAllTransitions()
            state.addTransitions(newTransitions)
        }
    }

    fun addErrorState() {
        val alphabet = _alphabet.toList()
        val errorState = newState()
        errorState.final = true

        _states.forEach { state ->
            alphabet.forEach { symbol ->
                if (state.getTransitions(symbol).isEmpty()) {
                    state.addTransition(Transition(symbol, errorState))
                }
            }
        }
    }

    fun removeEpsilons() {
        var hasChange: Boolean
        if (_alphabet.contains(Constants.EPSILON)){
            do {
                hasChange = false
                _states.forEach {currentState ->
                    val epsilonTransitions = currentState.getTransitions(Constants.EPSILON)

                    if (epsilonTransitions.isNotEmpty()) {
                        val nextState = epsilonTransitions[0].state

                        nextState.getTransitions().forEach{ transition ->
                            if (transition.symbol != Constants.EPSILON) {
                                hasChange = currentState.addTransition(transition) || hasChange
                            }
                        }

                        _states.forEach { state ->
                            if (state != nextState) {
                                state.getTransitions().forEach { transition ->
                                    if (transition.state == nextState && transition.symbol != Constants.EPSILON) {
                                        hasChange = state.addTransition(Transition(transition.symbol, currentState)) || hasChange
                                    }
                                }
                            }
                        }
                    }
                }
            } while (hasChange)

            _states.forEach { state ->
                val epsilonTransitions = state.getTransitions(Constants.EPSILON)
                state.removeTransitions(epsilonTransitions)
            }

            _alphabet.remove(Constants.EPSILON)
        }
    }

    fun toCSV() : String{
        if (_states.isEmpty()){
            return "Empty automata!"
        }else {
            sortAlphabet()

            val alphabetList = _alphabet.toList()

            val csv = StringBuilder()

            csv.append("State")

            alphabetList.forEach{
                csv.append(",$it")
            }
            csv.appendln()

            _states.forEach{state ->
                var stateType = ""
                if (state.final) { stateType = "*" }

                csv.append(stateType + " " + state.name)

                alphabetList.forEach {symbol ->
                    val transitions = state.getTransitions(symbol)
                    if (transitions.isNotEmpty() ){
                        var destStates = ""
                        transitions.forEach { destStates += "|${it.state.name}" }
                        destStates = destStates.substring(1)
                        csv.append(",$destStates")
                    } else {
                        csv.append(",-")
                    }
                }
                csv.appendln()
            }
            return csv.toString()
        }
    }

    private fun sortAlphabet() {
        _alphabet.sortWith(Comparator { o1, o2 ->
            if (o1 == Constants.EPSILON) return@Comparator 1
            if (o2 == Constants.EPSILON) return@Comparator -1
            o1!!.compareTo(o2!!)
        })
    }
}


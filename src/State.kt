class State(var name: String) {
    private var _transitions: ArrayList<Transition> = ArrayList()
    var initial: Boolean = false
    var final: Boolean = false

    fun newTransition(symbol: Char, state: State): Transition {
        val transition = Transition(symbol, state)
        _transitions.add(transition)
        return transition
    }

    fun addTransitions(transitions: ArrayList<Transition>) {
        _transitions.forEach { t ->
            if (transitions.contains(t))
                transitions.remove(t)
        }
        _transitions.addAll(transitions)
    }

    fun addTransition(transition: Transition): Boolean {
        return if (!_transitions.contains(transition)){
            _transitions.add(transition)
            true
        } else {
            false
        }
    }

    fun getTransitions(symbol: Char): ArrayList<Transition> {
        return ArrayList(_transitions.filter { it.symbol == symbol })
    }

    fun getTransitions(): ArrayList<Transition> {
        return ArrayList(_transitions.toList())
    }

    fun removeTransition(transition: Transition) {
        _transitions.remove(transition)
    }

    fun removeTransitions(transitions: ArrayList<Transition>) {
        _transitions.removeAll(transitions)
    }

    fun removeAllTransitions() {
        _transitions = ArrayList()
    }

    fun hasTransitions(): Boolean {
        return _transitions.isNotEmpty()
    }

}
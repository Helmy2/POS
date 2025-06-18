package com.wael.astimal.pos.core.base.mvi

/**
 * Defines a contract for a time-travel debugger.
 * This allows for storing and restoring states for debugging purposes.
 */
interface TimeCapsule<S : Reducer.ViewState> {
    fun addState(state: S)
    fun getStates(): List<S>
    fun selectState(index: Int)
}

/**
 * A concrete implementation of [TimeCapsule] that stores state history in memory.
 *
 * @param onStateSelected A lambda that is invoked when a past state is selected for time travel.
 */
class TimeTravelCapsule<S : Reducer.ViewState>(
    private val onStateSelected: (S) -> Unit
) : TimeCapsule<S> {
    private val states = mutableListOf<S>()

    override fun addState(state: S) {
        states.add(state)
    }

    override fun getStates(): List<S> {
        return states
    }

    override fun selectState(index: Int) {
        onStateSelected(states[index])
    }
}
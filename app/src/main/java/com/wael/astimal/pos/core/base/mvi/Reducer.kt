package com.wael.astimal.pos.core.base.mvi

/**
 * A Reducer is a pure function that takes the previous state and an event,
 * and returns a new state and an optional one-time effect. It contains no side effects.
 *
 * @param S The type of the ViewState.
 * @param E The type of the ViewEvent.
 * @param F The type of the ViewEffect.
 */
interface Reducer<S : Reducer.ViewState, E : Reducer.ViewEvent, F : Reducer.ViewEffect> {
    /** Marker interface for all states managed by the Reducer. */
    interface ViewState

    /** Marker interface for all events that can be processed. */
    interface ViewEvent

    /** Marker interface for all one-time side effects. */
    interface ViewEffect

    /**
     * Reduces the given current state with the given event into a new state and an optional effect.
     *
     * @param previousState The current state of the view.
     * @param event The event that should be processed.
     * @return A Pair containing the new state and an optional effect.
     */
    fun reduce(previousState: S, event: E): Pair<S, F?>
}
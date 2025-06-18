package com.wael.astimal.pos.core.base.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * An abstract ViewModel that implements the MVI (Model-View-Intent) pattern.
 *
 * @param S The type of the ViewState.
 * @param E The type of the ViewEvent.
 * @param F The type of the ViewEffect.
 * @property reducer The reducer responsible for state mutations.
 * @property initialState The initial state of the ViewModel.
 */
abstract class BaseViewModel<S : Reducer.ViewState, E : Reducer.ViewEvent, F : Reducer.ViewEffect>(
    private val reducer: Reducer<S, E, F>,
    initialState: S
) : ViewModel() {

    private val _state: MutableStateFlow<S> = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    private val _event: MutableSharedFlow<E> = MutableSharedFlow()
    val event = _event.asSharedFlow()

    private val _effect: Channel<F> = Channel()
    val effect = _effect.receiveAsFlow()

    val timeCapsule: TimeCapsule<S> = TimeTravelCapsule { storedState ->
        _state.tryEmit(storedState)
    }

    init {
        timeCapsule.addState(initialState)
        subscribeToEvents()
    }

    /**
     * This is the entry point for the View to send events to the ViewModel.
     */
    fun processEvent(event: E) {
        viewModelScope.launch { _event.emit(event) }
    }

    /**
     * Subscribes to the event flow and calls [handleEvent] for each incoming event.
     */
    private fun subscribeToEvents() {
        viewModelScope.launch {
            event.collect {
                handleEvent(it)
            }
        }
    }

    /**
     * The abstract method where concrete ViewModels handle business logic for each event.
     * This is where you would launch coroutines for async work.
     *
     * @param event The event to handle.
     */
    protected abstract fun handleEvent(event: E)

    /**
     * Sends the current state and an event to the reducer to calculate the new state and effect.
     * The new state is then emitted, and the effect is sent to the effect channel.
     * This should be called from within [handleEvent] for synchronous state updates.
     */
    protected fun setState(event: E) {
        val (newState, newEffect) = reducer.reduce(_state.value, event)
        if (_state.tryEmit(newState)) {
            timeCapsule.addState(newState)
        }
        newEffect?.let { _effect.trySend(it) }
    }
}
package com.wael.astimal.pos.features.client_management.presentaion.client

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update


class ClientViewModel : ViewModel() {

    private val _state = MutableStateFlow(ClientState())
    val state = _state

    fun handleEvent(event: ClientEvent) {
        when (event) {
            is ClientEvent.UpdateSelectDestination -> updateSelectDestination(event.destination)
        }
    }

    private fun updateSelectDestination(destination: ClientDestination) {
        _state.update { it.copy(selectedDestination = destination) }
    }
}
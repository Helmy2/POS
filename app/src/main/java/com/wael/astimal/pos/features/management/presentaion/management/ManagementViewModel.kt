package com.wael.astimal.pos.features.management.presentaion.management

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update


class ManagementViewModel : ViewModel() {

    private val _state = MutableStateFlow(ManagementState())
    val state = _state

    fun handleEvent(event: ManagementEvent) {
        when (event) {
            is ManagementEvent.UpdateSelectDestination -> updateSelectDestination(event.destination)
        }
    }

    private fun updateSelectDestination(destination: ManagementDestination) {
        _state.update { it.copy(selectedDestination = destination) }
    }
}
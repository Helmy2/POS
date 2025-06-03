package com.wael.astimal.pos.features.inventory.presentation.inventory

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class InventoryViewModel : ViewModel() {

    private val _state = MutableStateFlow(InventoryState())
    val state = _state

    fun handleEvent(event: InventoryEvent) {
        when (event) {
            is InventoryEvent.UpdateSelectDestination -> updateSelectDestination(event.destination)
        }
    }

    private fun updateSelectDestination(destination: InventoryDestination) {
        _state.update { it.copy(selectedDestination = destination) }
    }
}
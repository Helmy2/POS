package com.wael.astimal.pos.features.inventory.presentation.inventory

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InventoryViewModel : ViewModel() {

    private val _state = MutableStateFlow(InventoryState())
    val state: StateFlow<InventoryState> = _state.asStateFlow()

    init {
        _state.value = InventoryState(items = InventoryDestinations.getAll())
    }
}

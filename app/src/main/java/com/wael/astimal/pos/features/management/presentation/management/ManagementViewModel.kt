package com.wael.astimal.pos.features.management.presentation.management

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class ManagementViewModel : ViewModel() {

    private val _state = MutableStateFlow(ManagementState())
    val state: StateFlow<ManagementState> = _state.asStateFlow()

    init {
        _state.value = ManagementState(items = ManagementDestinations.getAll())
    }
}

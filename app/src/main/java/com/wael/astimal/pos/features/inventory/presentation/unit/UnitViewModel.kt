package com.wael.astimal.pos.features.inventory.presentation.unit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UnitViewModel() : ViewModel() {

    private val _state = MutableStateFlow(
        UnitDetailsState()
    )

    val state = _state.onStart {
        search("")
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = UnitDetailsState()
    )

    private fun search(query: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            delay(100) // Simulate network delay
            // todo: Replace with actual search logic
            _state.update { it.copy(loading = false) }
        }
    }

    fun handleEvent(event: UnitEvent) {
        when (event) {
            is UnitEvent.CreateUnitOfMeasure -> createUnitOfMeasure()
            is UnitEvent.DeleteUnitOfMeasure -> deleteUnitOfMeasure()
            is UnitEvent.UpdateUnitOfMeasure -> updateUnitOfMeasure()
            is UnitEvent.Search -> search(event.query)
            is UnitEvent.UpdateIsQueryActive -> updateIsQueryActive(event.isQueryActive)
            is UnitEvent.UpdateQuery -> updateQuery(event.query)
            is UnitEvent.UpdateArName -> updateArName(event.name)
            is UnitEvent.UpdateEnName -> updateEnName(event.name)
            is UnitEvent.UpdateRate -> updateRate(event.rate)
            is UnitEvent.Select -> selectUnit()
        }
    }

    private fun selectUnit() {
        // todo: Implement logic to select a unit of measure
    }

    private fun updateQuery(query: String) {
        _state.update { it.copy(query = query) }
        search(query)
        _state.update { it.copy(loading = false) }
    }

    private fun updateIsQueryActive(queryActive: Boolean) {
        _state.update { it.copy(isQueryActive = queryActive) }
    }

    private fun updateArName(name: String) {
        _state.update { it.copy(arName = name) }
    }

    private fun updateEnName(name: String) {
        _state.update { it.copy(enName = name) }
    }

    private fun updateRate(rate: String) {
        _state.update { it.copy(rate = rate) }
    }


    private fun updateUnitOfMeasure() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            delay(100)
            // todo: Replace with actual update logic
            _state.update { it.copy(loading = false) }
        }
    }

    private fun deleteUnitOfMeasure() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            delay(100)
            // todo : Replace with actual delete logic
            _state.update { it.copy(loading = false) }
        }
    }

    private fun createUnitOfMeasure() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            delay(100)
            // todo: Replace with actual create logic
            _state.update { it.copy(loading = false) }
        }
    }
}
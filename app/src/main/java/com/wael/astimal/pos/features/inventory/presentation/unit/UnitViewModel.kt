package com.wael.astimal.pos.features.inventory.presentation.unit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.domain.entity.UnitDetails
import com.wael.astimal.pos.features.inventory.domain.entity.toUnitDetails
import com.wael.astimal.pos.features.inventory.domain.repository.UnitRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UnitViewModel(
    private val unitRepository: UnitRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UnitDetailsState())
    val state: StateFlow<UnitDetailsState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        handleEvent(UnitEvent.Search(""))
    }

    fun handleEvent(event: UnitEvent) {
        when (event) {
            is UnitEvent.CreateUnit -> createUnitFromState()
            is UnitEvent.UpdateUnit -> updateUnitFromState()
            is UnitEvent.DeleteUnit -> deleteSelectedUnit()
            is UnitEvent.NewUnit -> handleSelectUnit(null)
            is UnitEvent.Search -> searchUnits(event.query)
            is UnitEvent.Select -> handleSelectUnit(event.unit)
            is UnitEvent.UpdateQuery -> {
                _state.update { it.copy(query = event.query) }
                searchUnits(event.query)
            }
            is UnitEvent.UpdateIsQueryActive -> _state.update { it.copy(isQueryActive = event.isQueryActive) }
            is UnitEvent.UpdateArName -> _state.update { it.copy(arName = event.name) }
            is UnitEvent.UpdateEnName -> _state.update { it.copy(enName = event.name) }
            is UnitEvent.UpdateRate -> _state.update { it.copy(rate = event.rate) }
        }
    }

    private fun searchUnits(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            if (query.length > 2 || query.isEmpty()) {
                delay(300)
            }
            unitRepository.getUnits(query)
                .map { entities -> entities.map { it.toUnitDetails() } }
                .catch { e ->
                    _state.update { it.copy(loading = false, error = "Error fetching units: ${e.message}") }
                }
                .collect { unitDetailsList ->
                    _state.update { it.copy(loading = false, searchResults = unitDetailsList) }
                }
        }
    }

    private fun handleSelectUnit(unitDetails: UnitDetails?) {
        if (unitDetails == null) {
            _state.update {
                it.copy(
                    selectedUnit = null,
                    arName = "",
                    enName = "",
                    rate = "1"
                )
            }
        } else {
            _state.update {
                it.copy(
                    selectedUnit = unitDetails,
                    arName = unitDetails.arName,
                    enName = unitDetails.enName,
                    rate = unitDetails.rate.toString()
                )
            }
        }
    }

    private fun createUnitFromState() {
        val currentState = _state.value
        if (currentState.arName.isBlank() && currentState.enName.isBlank()) {
            _state.update { it.copy(error = "At least one name (Arabic or English) is required.") }
            return
        }
        if (currentState.rate.isBlank()) {
            _state.update { it.copy(error = "Rate is required.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val newUnitEntity = UnitEntity(
                serverId = null,
                arName = currentState.arName,
                enName = currentState.enName,
                rate = currentState.rate.toFloatOrNull() ?: 1f,
                isSynced = false,
                lastModified = System.currentTimeMillis(),
                isDeletedLocally = false
            )
            val result = unitRepository.addUnit(newUnitEntity)
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            loading = false,
                            arName = "",
                            enName = "",
                            rate = "1"
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(loading = false, error = "Failed to create unit: ${e.message}") }
                }
            )
        }
    }

    private fun updateUnitFromState() {
        val currentState = _state.value
        val unitToUpdate = currentState.selectedUnit
        if (unitToUpdate == null) {
            _state.update { it.copy(error = "No unit selected for update.") }
            return
        }
        if (currentState.arName.isBlank() && currentState.enName.isBlank()) {
            _state.update { it.copy(error = "At least one name (Arabic or English) is required.") }
            return
        }
        if (currentState.rate.isBlank()) {
            _state.update { it.copy(error = "Rate is required.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val updatedUnitEntity = UnitEntity(
                localId = unitToUpdate.localId,
                serverId = unitToUpdate.serverId,
                arName = currentState.arName,
                enName = currentState.enName,
                rate = currentState.rate.toFloatOrNull() ?: 1f,
                isSynced = false,
                lastModified = System.currentTimeMillis(),
                isDeletedLocally = unitToUpdate.isDeletedLocally
            )
            val result = unitRepository.updateUnit(updatedUnitEntity)
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            loading = false,
                            selectedUnit = null,
                            arName = "",
                            enName = "",
                            rate = "1"
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(loading = false, error = "Failed to update unit: ${e.message}") }
                }
            )
        }
    }

    private fun deleteSelectedUnit() {
        val unitToDeleteDetails = _state.value.selectedUnit
        if (unitToDeleteDetails == null) {
            _state.update { it.copy(error = "No unit selected for deletion.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val unitEntityToDelete = UnitEntity(
                localId = unitToDeleteDetails.localId,
                serverId = unitToDeleteDetails.serverId,
                arName = unitToDeleteDetails.arName,
                enName = unitToDeleteDetails.enName,
                rate = unitToDeleteDetails.rate
            )

            val result = unitRepository.deleteUnit(unitEntityToDelete)
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            loading = false,
                            selectedUnit = null,
                            arName = "",
                            enName = "",
                            rate = "1"
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(loading = false, error = "Failed to delete unit: ${e.message}") }
                }
            )
        }
    }

    fun triggerSync() {
        // todo: Implement sync logic
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val syncResult = unitRepository.syncUnits()
            syncResult.fold(
                onSuccess = {
                    _state.update { it.copy(loading = false) }
                },
                onFailure = { e ->
                    _state.update { it.copy(loading = false, error = "Sync failed: ${e.message}") }
                }
            )
        }
    }
}
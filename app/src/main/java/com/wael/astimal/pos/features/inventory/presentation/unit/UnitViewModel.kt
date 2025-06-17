package com.wael.astimal.pos.features.inventory.presentation.unit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.snackbar.UiEvent
import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import com.wael.astimal.pos.features.inventory.domain.repository.UnitRepository
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UnitViewModel(
    private val unitRepository: UnitRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(UnitDetailsState())
    val state: StateFlow<UnitDetailsState> = _state.asStateFlow()

    private var searchJob: Job? = null

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            sessionManager.getCurrentUser().collect { user ->
                _state.update { it.copy(currentUser = user) }
            }
        }
        handleEvent(UnitEvent.Search(""))
    }

    fun handleEvent(event: UnitEvent) {
        when (event) {
            is UnitEvent.CreateUnit -> createUnitFromState()
            is UnitEvent.UpdateUnit -> updateUnitFromState()
            is UnitEvent.DeleteUnit -> deleteSelectedUnit()
            is UnitEvent.NewUnit -> handleSelectUnit(null)
            is UnitEvent.Search -> searchUnits(event.query)
            is UnitEvent.Select -> handleSelectUnit(event.productUnit)
            is UnitEvent.UpdateQuery -> {
                _state.update { it.copy(query = event.query) }
                searchUnits(event.query)
            }

            is UnitEvent.UpdateIsQueryActive -> _state.update { it.copy(isQueryActive = event.isQueryActive) }
            is UnitEvent.UpdateArName -> _state.update { it.copy(arName = event.name) }
            is UnitEvent.UpdateEnName -> _state.update { it.copy(enName = event.name) }
        }
    }

    private fun searchUnits(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            if (query.length > 2 || query.isEmpty()) {
                delay(300)
            }
            unitRepository.getUnits(query).map { entities -> entities.map { it.toDomain() } }
                .catch { e ->
                    _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_fetching_units))
                }.collect { unitDetailsList ->
                    _state.update { it.copy(loading = false, searchResults = unitDetailsList) }
                }
        }
    }

    private fun handleSelectUnit(productUnit: ProductUnit?) {
        if (productUnit == null) {
            _state.update {
                it.copy(
                    selectedProductUnit = null, arName = "", enName = "",
                )
            }
        } else {
            _state.update {
                it.copy(
                    selectedProductUnit = productUnit,
                    arName = productUnit.localizedName.arName ?: "",
                    enName = productUnit.localizedName.enName ?: "",
                )
            }
        }
    }

    private fun createUnitFromState() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState.arName.isBlank() && currentState.enName.isBlank()) {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.at_least_one_name_arabic_or_english_is_required))
                return@launch
            }

            _state.update { it.copy(loading = true) }
            val newUnitEntity = UnitEntity(
                serverId = null,
                arName = currentState.arName,
                enName = currentState.enName,
                isSynced = false,
                updatedAt = System.currentTimeMillis(),
                isDeletedLocally = false
            )
            val result = unitRepository.addUnit(newUnitEntity)
            result.fold(onSuccess = {
                _state.update {
                    it.copy(
                        loading = false, arName = "", enName = ""
                    )
                }
            }, onFailure = { e ->
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.failed_to_create_unit))
            })
        }
    }

    private fun updateUnitFromState() {
        viewModelScope.launch {
            val currentState = _state.value
            val unitToUpdate = currentState.selectedProductUnit
            if (unitToUpdate == null) {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.no_unit_selected_for_update))
                return@launch
            }
            if (currentState.arName.isBlank() && currentState.enName.isBlank()) {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.at_least_one_name_arabic_or_english_is_required))
                return@launch
            }

            _state.update { it.copy(loading = true) }
            val updatedUnitEntity = UnitEntity(
                localId = unitToUpdate.id.local,
                serverId = unitToUpdate.id.server,
                arName = currentState.arName,
                enName = currentState.enName,
                isSynced = false,
                updatedAt = System.currentTimeMillis(),
            )
            val result = unitRepository.updateUnit(updatedUnitEntity)
            result.fold(onSuccess = {
                _state.update {
                    it.copy(
                        loading = false,
                        selectedProductUnit = null,
                        arName = "",
                        enName = "",
                    )
                }
            }, onFailure = { e ->
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.failed_to_update_unit))
            })
        }
    }

    private fun deleteSelectedUnit() {
        viewModelScope.launch {
            val unitToDeleteDetails = _state.value.selectedProductUnit
            if (unitToDeleteDetails == null) {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.no_unit_selected_for_deletion))
                return@launch
            }

            _state.update { it.copy(loading = true) }
            val unitEntityToDelete = UnitEntity(
                localId = unitToDeleteDetails.id.local,
                serverId = unitToDeleteDetails.id.server,
                arName = unitToDeleteDetails.localizedName.arName,
                enName = unitToDeleteDetails.localizedName.enName,
            )

            val result = unitRepository.deleteUnit(unitEntityToDelete)
            result.fold(onSuccess = {
                _state.update {
                    it.copy(
                        loading = false,
                        selectedProductUnit = null,
                        arName = "",
                        enName = "",
                    )
                }
            }, onFailure = { e ->
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.failed_to_delete_unit))
            })
        }
    }
}
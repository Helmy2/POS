package com.wael.astimal.pos.features.inventory.presentation.unit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.base.UiEvent
import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import com.wael.astimal.pos.features.inventory.domain.repository.UnitRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
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
    private val unitRepository: UnitRepository, private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UnitDetailsState())
    val state: StateFlow<UnitDetailsState> = _state.asStateFlow()

    private var searchJob: Job? = null

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            userRepository.getCurrentUser()?.let { user ->
                _state.update { it.copy(currentUser = user) }
            }
        }
        handleEvent(UnitEvent.Search(""))
    }

    fun handleEvent(event: UnitEvent) {
        when (event) {
            is UnitEvent.CreateUnit -> saveUnit()
            is UnitEvent.UpdateUnit -> saveUnit()
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

    private fun saveUnit() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState.arName.isBlank() && currentState.enName.isBlank()) {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.at_least_one_name_arabic_or_english_is_required))
                return@launch
            }

            _state.update { it.copy(loading = true) }
            val result = unitRepository.saveUnit(
                UnitEntity(
                    localId = currentState.selectedProductUnit?.id?.local ?: 0L,
                    serverId = currentState.selectedProductUnit?.id?.server ?: 0L,
                    arName = currentState.arName,
                    enName = currentState.enName,
                    createdAt = currentState.selectedProductUnit?.createdAt
                        ?: System.currentTimeMillis(),
                )
            )
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
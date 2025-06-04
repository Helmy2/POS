package com.wael.astimal.pos.features.inventory.presentation.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.features.inventory.data.entity.StoreType
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StoreViewModel(
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StoreState())
    val state: StateFlow<StoreState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        onEvent(StoreEvent.Search(""))
    }

    fun onEvent(event: StoreEvent) {
        when (event) {
            is StoreEvent.CreateStore -> saveStore()
            is StoreEvent.UpdateStore -> saveStore()
            is StoreEvent.DeleteStore -> deleteSelectedStore()
            is StoreEvent.Search -> searchStores(event.query)
            is StoreEvent.SelectStore -> handleSelectStore(event.store)
            is StoreEvent.UpdateQuery -> {
                _state.update { it.copy(query = event.query) }
                searchStores(event.query)
            }
            is StoreEvent.UpdateIsQueryActive -> _state.update { it.copy(isQueryActive = event.isQueryActive) }
            is StoreEvent.UpdateInputArName -> _state.update { it.copy(inputArName = event.name) }
            is StoreEvent.UpdateInputEnName -> _state.update { it.copy(inputEnName = event.name) }
            is StoreEvent.UpdateInputType -> _state.update { it.copy(inputType = event.type) }
        }
    }

    private fun searchStores(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            if (query.length > 1 || query.isEmpty()) {
                delay(300)
            }
            storeRepository.getStores(query)
                .catch { e ->
                    _state.update { it.copy(loading = false, error = "Error fetching stores: ${e.message}") }
                }
                .collect { stores ->
                    _state.update { it.copy(loading = false, searchResults = stores) }
                }
        }
    }

    private fun handleSelectStore(store: Store?) {
        if (store == null) {
            _state.update {
                it.copy(
                    selectedStore = null,
                    inputArName = "",
                    inputEnName = "",
                    inputType = StoreType.SUB
                )
            }
        } else { // Select existing for editing
            _state.update {
                it.copy(
                    selectedStore = store,
                    inputArName = store.arName ?: "",
                    inputEnName = store.enName ?: "",
                    inputType = store.type
                )
            }
        }
    }

    private fun saveStore() {
        val currentState = _state.value
        if (currentState.inputArName.isBlank() && currentState.inputEnName.isBlank()) {
            _state.update { it.copy(error = "At least one name (Arabic or English) is required.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }

            val result = if (currentState.isNew || currentState.selectedStore == null) {
                storeRepository.addStore(
                    arName = currentState.inputArName,
                    enName = currentState.inputEnName,
                    type = currentState.inputType
                )
            } else {
                storeRepository.updateStore(
                    store = currentState.selectedStore,
                    newArName = currentState.inputArName,
                    newEnName = currentState.inputEnName,
                    newType = currentState.inputType
                )
            }

            result.fold(
                onSuccess = { savedStore ->
                    _state.update {
                        it.copy(
                            loading = false,
                            selectedStore = null,
                            inputArName = "",
                            inputEnName = "",
                            inputType = StoreType.SUB
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(loading = false, error = "Failed to save store: ${e.message}") }
                }
            )
        }
    }

    private fun deleteSelectedStore() {
        val storeToDelete = _state.value.selectedStore
        if (storeToDelete == null) {
            _state.update { it.copy(error = "No store selected for deletion.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val result = storeRepository.deleteStore(storeToDelete)
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            loading = false,
                            selectedStore = null,
                            inputArName = "",
                            inputEnName = "",
                            inputType = StoreType.SUB
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(loading = false, error = "Failed to delete store: ${e.message}") }
                }
            )
        }
    }

    private fun triggerSyncStores() {
        // todo: Implement actual sync logic
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val result = storeRepository.syncStores()
            result.fold(
                onSuccess = { _state.update { it.copy(loading = false, error = "Sync successful (placeholder)") } },
                onFailure = { e -> _state.update { it.copy(loading = false, error = "Sync failed: ${e.message}") } }
            )
        }
    }
}
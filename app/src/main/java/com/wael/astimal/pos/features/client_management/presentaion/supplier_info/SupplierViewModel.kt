package com.wael.astimal.pos.features.client_management.presentaion.supplier_info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.client_management.domain.repository.SupplierRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SupplierViewModel(
    private val clientRepository: SupplierRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SupplierInfoState())
    val state: StateFlow<SupplierInfoState> = _state.asStateFlow()

    private var clientSearchJob: Job? = null

    init {
        onEvent(SupplierInfoEvent.SearchSuppliers(_state.value.query))
    }

    fun onEvent(event: SupplierInfoEvent) {
        when (event) {
            is SupplierInfoEvent.SearchSuppliers -> searchSuppliersList(event.query)
            is SupplierInfoEvent.SelectSupplier -> {
                _state.update {
                    it.copy(
                        selectedSupplier = event.supplier,
                        showDetailDialog = event.supplier != null
                    )
                }
            }

            is SupplierInfoEvent.ClearSnackbar -> _state.update { it.copy(snackbarMessage = null) }
            is SupplierInfoEvent.UpdateQuery -> {
                _state.update { it.copy(query = event.query) }
                searchSuppliersList(event.query)
            }
            SupplierInfoEvent.DetailSupplier -> _state.update { it.copy(showDetailDialog = false) }
            SupplierInfoEvent.ShowDetailDialog -> _state.update { it.copy(showDetailDialog = true) }
        }
    }

    private fun searchSuppliersList(query: String) {
        clientSearchJob?.cancel()
        clientSearchJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, query = query) }
            if (query.length > 1 || query.isEmpty()) {
                delay(300)
            }
            clientRepository.searchSupplier(query)
                .catch { e ->
                    _state.update {
                        it.copy(
                            loading = false,
                            error = R.string.error_searching_clients
                        )
                    }
                }
                .collect { clients ->
                    _state.update { it.copy(loading = false, searchResults = clients) }
                }
        }
    }
}
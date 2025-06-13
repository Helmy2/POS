package com.wael.astimal.pos.features.management.presentation.client_info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.management.domain.repository.ClientRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClientInfoViewModel(
    private val clientRepository: ClientRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ClientInfoState())
    val state: StateFlow<ClientInfoState> = _state.asStateFlow()

    private var clientSearchJob: Job? = null

    init {
        onEvent(ClientInfoEvent.SearchClients(_state.value.query))
    }

    fun onEvent(event: ClientInfoEvent) {
        when (event) {
            is ClientInfoEvent.SearchClients -> searchClientsList(event.query)
            is ClientInfoEvent.SelectClient -> {
                _state.update {
                    it.copy(
                        selectedClient = event.client,
                        showDetailDialog = event.client != null
                    )
                }
            }

            is ClientInfoEvent.ClearSnackbar -> _state.update { it.copy(snackbarMessage = null) }
            is ClientInfoEvent.UpdateQuery -> {
                _state.update { it.copy(query = event.query) }
                searchClientsList(event.query)
            }
            ClientInfoEvent.DetailClient -> _state.update { it.copy(showDetailDialog = false) }
            ClientInfoEvent.ShowDetailDialog -> _state.update { it.copy(showDetailDialog = true) }
        }
    }

    private fun searchClientsList(query: String) {
        clientSearchJob?.cancel()
        clientSearchJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, query = query) }
            if (query.length > 1 || query.isEmpty()) {
                delay(300)
            }
            clientRepository.searchClients(query)
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
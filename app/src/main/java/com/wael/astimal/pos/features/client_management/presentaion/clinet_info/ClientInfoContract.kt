package com.wael.astimal.pos.features.client_management.presentaion.clinet_info

import com.wael.astimal.pos.features.client_management.domain.entity.Client

data class ClientInfoState(
    val loading: Boolean = false,
    val searchResults: List<Client> = emptyList(),
    val selectedClient: Client? = null,
    val query: String = "",
    val error: String? = null,
    val snackbarMessage: String? = null,
    val showDetailDialog: Boolean = false,
)

sealed interface ClientInfoEvent {
    data class SearchClients(val query: String) : ClientInfoEvent
    data class SelectClient(val client: Client?) : ClientInfoEvent
    data object ClearSnackbar : ClientInfoEvent
    data class UpdateQuery(val query: String) : ClientInfoEvent
    data object DetailClient: ClientInfoEvent
    data object ShowDetailDialog : ClientInfoEvent
}
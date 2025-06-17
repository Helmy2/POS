package com.wael.astimal.pos.features.inventory.presentation.store

import com.wael.astimal.pos.features.inventory.data.entity.StoreType
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.user.domain.entity.User

data class StoreState(
    val loading: Boolean = false,
    val searchResults: List<Store> = emptyList(),
    val selectedStore: Store? = null,
    val inputArName: String = "",
    val inputEnName: String = "",
    val inputType: StoreType? = null,
    val query: String = "",
    val isQueryActive: Boolean = false,
    val currentUser: User? = null,
) {
    val isNew: Boolean get() = selectedStore == null
    val canEdit get() = currentUser?.isAdmin == true
}

sealed interface StoreEvent {
    data object CreateStore : StoreEvent
    data object UpdateStore : StoreEvent
    data object DeleteStore : StoreEvent
    data class UpdateInputArName(val name: String) : StoreEvent
    data class UpdateInputEnName(val name: String) : StoreEvent
    data class UpdateInputType(val type: StoreType?) : StoreEvent
    data class UpdateQuery(val query: String) : StoreEvent
    data class UpdateIsQueryActive(val isQueryActive: Boolean) : StoreEvent
    data class Search(val query: String) : StoreEvent
    data class SelectStore(val store: Store?) : StoreEvent
}
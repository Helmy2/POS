package com.wael.astimal.pos.features.inventory.presentation.store

import com.wael.astimal.pos.features.inventory.data.entity.StoreType
import com.wael.astimal.pos.features.inventory.domain.entity.Store

data class StoreState(
    val loading: Boolean = false,
    val searchResults: List<Store> = emptyList(),
    val selectedStore: Store? = null,
    val inputArName: String = "",
    val inputEnName: String = "",
    val inputType: StoreType? = null,
    val query: String = "",
    val isQueryActive: Boolean = false,
    val error: String? = null
) {
    val isNew: Boolean get() = selectedStore == null
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
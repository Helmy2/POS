package com.wael.astimal.pos.features.inventory.presentation.unit

import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit

sealed interface UnitEvent {
    data object CreateUnit : UnitEvent
    data object UpdateUnit : UnitEvent
    data object DeleteUnit : UnitEvent
    data object NewUnit : UnitEvent

    data class UpdateRate(val rate: String) : UnitEvent
    data class UpdateArName(val name: String) : UnitEvent
    data class UpdateEnName(val name: String) : UnitEvent
    data class UpdateQuery(val query: String) : UnitEvent
    data class UpdateIsQueryActive(val isQueryActive: Boolean) : UnitEvent
    data class Search(val query: String) : UnitEvent
    data class Select(val productUnit: ProductUnit) : UnitEvent
}

data class UnitDetailsState(
    val loading: Boolean = false,
    val searchResults: List<ProductUnit> = emptyList(),
    val selectedProductUnit: ProductUnit? = null,
    val rate: String = "1",
    val arName: String = "",
    val enName: String = "",
    val query: String = "",
    val isQueryActive: Boolean = false,
    val error: String? = null,
) {
    val isNew: Boolean get() = selectedProductUnit == null
}
package com.wael.astimal.pos.features.inventory.presentation.unit

import com.wael.astimal.pos.features.inventory.domain.entity.UnitDetails

sealed interface UnitEvent {
    data object CreateUnitOfMeasure : UnitEvent
    data object UpdateUnitOfMeasure : UnitEvent
    data object DeleteUnitOfMeasure : UnitEvent

    data class UpdateRate(val rate: String) : UnitEvent
    data class UpdateArName(val name: String) : UnitEvent
    data class UpdateEnName(val name: String) : UnitEvent
    data class UpdateQuery(val query: String) : UnitEvent
    data class UpdateIsQueryActive(val isQueryActive: Boolean) : UnitEvent
    data class Search(val query: String) : UnitEvent
    data class Select(val unit: UnitDetails) : UnitEvent
}

data class UnitDetailsState(
    val loading: Boolean = false,
    val searchResults: List<UnitDetails> = emptyList(),
    val selectedUnit: UnitDetails? = null,
    val rate: String = "1",
    val arName: String = "",
    val enName: String = "",
    val query: String = "",
    val isQueryActive: Boolean = false,
) {
    val isNew: Boolean get() = selectedUnit == null
}
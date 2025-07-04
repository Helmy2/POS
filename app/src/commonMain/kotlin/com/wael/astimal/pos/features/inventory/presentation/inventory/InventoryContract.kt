package com.wael.astimal.pos.features.inventory.presentation.inventory


import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.domain.navigation.Destination
import org.jetbrains.compose.resources.StringResource

object InventoryContract {
    data class InventoryItem(val destination: Destination, val label: StringResource)

    data class State(
        val items: List<InventoryItem> = emptyList()
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data object LoadInventoryItems : Event
        data class ItemClicked(val destination: Destination) : Event
    }
}
package com.wael.astimal.pos.features.inventory.presentation.inventory


import androidx.annotation.StringRes
import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.domain.navigation.Destination

object InventoryContract {
    data class InventoryItem(val destination: Destination, @StringRes val label: Int)

    data class State(
        val items: List<InventoryItem> = emptyList()
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data object LoadInventoryItems : Event
        data class ItemClicked(val destination: Destination) : Event
    }
}
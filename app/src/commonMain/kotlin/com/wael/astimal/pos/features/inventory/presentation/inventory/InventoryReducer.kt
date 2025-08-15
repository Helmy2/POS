package com.wael.astimal.pos.features.inventory.presentation.inventory

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.domain.navigation.Destination
import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.categories
import pos.app.generated.resources.employee
import pos.app.generated.resources.products
import pos.app.generated.resources.stock_management
import pos.app.generated.resources.stock_transfer
import pos.app.generated.resources.stores
import pos.app.generated.resources.units

class InventoryReducer : Reducer<InventoryReducer.State, InventoryReducer.Event, Nothing> {
    data class InventoryItem(val destination: Destination, val label: StringResource)

    data class State(
        val items: List<InventoryItem> = emptyList()
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data object LoadInventoryItems : Event
        data class ItemClicked(val destination: Destination) : Event
    }
    override fun reduce(
        previousState: State,
        event: Event
    ): Pair<State, Nothing?> {
        return when (event) {
            is Event.LoadInventoryItems -> {
                val inventoryItems = listOf(
                    InventoryItem(
                        Destination.Employee,
                        Res.string.employee
                    ),
                    InventoryItem(Destination.Stores, Res.string.stores),
                    InventoryItem(Destination.Units, Res.string.units),
                    InventoryItem(Destination.Categories, Res.string.categories),
                    InventoryItem(Destination.Products, Res.string.products),
                    InventoryItem(
                        Destination.StockManagement,
                        Res.string.stock_management
                    ),
                    InventoryItem(
                        Destination.StockTransfer(),
                        Res.string.stock_transfer
                    ),
                )
                previousState.copy(items = inventoryItems) to null
            }

            is Event.ItemClicked -> previousState to null
        }
    }
}

package com.wael.astimal.pos.features.inventory.presentation.inventory

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.features.user.domain.PermissionManager
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
                val inventoryItems =
                    buildList {
                        if (PermissionManager.isAdmin())
                            add(InventoryItem(Destination.Employee, Res.string.employee))
                        if (PermissionManager.canView(Destination.Stores))
                            add(InventoryItem(Destination.Stores, Res.string.stores))
                        if (PermissionManager.canView(Destination.Units))
                            add(InventoryItem(Destination.Units, Res.string.units))
                        if (PermissionManager.canView(Destination.Categories))
                            add(InventoryItem(Destination.Categories, Res.string.categories))
                        if (PermissionManager.canView(Destination.Products))
                            add(InventoryItem(Destination.Products, Res.string.products))
                        if (PermissionManager.canView(Destination.StockManagement))
                            add(
                                InventoryItem(
                                    Destination.StockManagement,
                                    Res.string.stock_management
                                )
                            )
                        if (PermissionManager.canView(Destination.StockTransfer()))
                            add(
                                InventoryItem(
                                    Destination.StockTransfer(),
                                    Res.string.stock_transfer
                                )
                            )
                    }
                previousState.copy(items = inventoryItems) to null
            }

            is Event.ItemClicked -> previousState to null
        }
    }
}

package com.wael.astimal.pos.features.inventory.presentation.inventory

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.domain.navigation.Destination
import pos.app.generated.resources.Res
import pos.app.generated.resources.categories
import pos.app.generated.resources.products
import pos.app.generated.resources.stock_management
import pos.app.generated.resources.stock_transfer
import pos.app.generated.resources.stores
import pos.app.generated.resources.units

class InventoryReducer : Reducer<InventoryContract.State, InventoryContract.Event, Nothing> {
    override fun reduce(
        previousState: InventoryContract.State,
        event: InventoryContract.Event
    ): Pair<InventoryContract.State, Nothing?> {
        return when (event) {
            is InventoryContract.Event.LoadInventoryItems -> {
                val inventoryItems = listOf(
                    InventoryContract.InventoryItem(Destination.Stores, Res.string.stores),
                    InventoryContract.InventoryItem(Destination.Units, Res.string.units),
                    InventoryContract.InventoryItem(Destination.Categories, Res.string.categories),
                    InventoryContract.InventoryItem(Destination.Products, Res.string.products),
                    InventoryContract.InventoryItem(
                        Destination.StockTransfer,
                        Res.string.stock_transfer
                    ),
                    InventoryContract.InventoryItem(
                        Destination.StockManagement,
                        Res.string.stock_management
                    )
                )
                previousState.copy(items = inventoryItems) to null
            }

            is InventoryContract.Event.ItemClicked -> previousState to null
        }
    }
}

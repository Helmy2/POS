package com.wael.astimal.pos.features.inventory.presentation.inventory

import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.domain.navigation.Destination

class InventoryReducer : Reducer<InventoryContract.State, InventoryContract.Event, Nothing> {
    override fun reduce(
        previousState: InventoryContract.State,
        event: InventoryContract.Event
    ): Pair<InventoryContract.State, Nothing?> {
        return when (event) {
            is InventoryContract.Event.LoadInventoryItems -> {
                val inventoryItems = listOf(
                    InventoryContract.InventoryItem(Destination.Stores, R.string.stores),
                    InventoryContract.InventoryItem(Destination.Units, R.string.units),
                    InventoryContract.InventoryItem(Destination.Categories, R.string.categories),
                    InventoryContract.InventoryItem(Destination.Products, R.string.products),
                    InventoryContract.InventoryItem(
                        Destination.StockTransfer,
                        R.string.stock_transfer
                    ),
                    InventoryContract.InventoryItem(
                        Destination.StockManagement,
                        R.string.stock_management
                    )
                )
                previousState.copy(items = inventoryItems) to null
            }

            is InventoryContract.Event.ItemClicked -> previousState to null
        }
    }
}

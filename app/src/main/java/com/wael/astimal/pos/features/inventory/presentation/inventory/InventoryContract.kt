package com.wael.astimal.pos.features.inventory.presentation.inventory

import androidx.annotation.StringRes
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.domain.navigation.Destination

data class InventoryItem(val destination: Destination, @StringRes val label: Int)


data class InventoryState(
    val items: List<InventoryItem> = emptyList()
)


object InventoryDestinations {
    fun getAll(): List<InventoryItem> =
        listOf(
            InventoryItem(Destination.Stores, R.string.stores),
            InventoryItem(Destination.Units, R.string.units),
            InventoryItem(Destination.Categories, R.string.categories),
            InventoryItem(Destination.Products, R.string.products),
            InventoryItem(Destination.StockTransfer, R.string.stock_transfer),
            InventoryItem(Destination.StockManagement, R.string.stock_management)
        )
}

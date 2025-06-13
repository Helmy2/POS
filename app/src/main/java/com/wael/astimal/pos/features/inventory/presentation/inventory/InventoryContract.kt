package com.wael.astimal.pos.features.inventory.presentation.inventory


sealed interface InventoryEvent {
    data class UpdateSelectDestination(val destination: InventoryDestination) : InventoryEvent
}

data class InventoryState(
    val selectedDestination: InventoryDestination? = null,
)

enum class InventoryDestination {
    UnitOfMeasures, Stores, Categories, Products, StockTransfer, StockManagement;

    companion object {
        fun getAll(): List<InventoryDestination> =
            listOf(Stores, UnitOfMeasures, Categories, Products, StockTransfer, StockManagement)
    }
}
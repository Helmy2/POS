package com.wael.astimal.pos.features.inventory.presentation.stock_management

import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.inventory.domain.entity.StoreStock
import com.wael.astimal.pos.features.user.domain.entity.User


data class StockManagementState(
    val loading: Boolean = false,
    val stores: List<Store> = emptyList(),
    val stocks: List<StoreStock> = emptyList(),
    val query: String = "",
    val selectedStore: Store? = null,
    val currentUser: User? = null,

    val showAdjustmentDialog: Boolean = false,
    val adjustmentTarget: StoreStock? = null,
    val adjustmentQuantityChange: String = "",
    val adjustmentReason: StockAdjustmentReason = StockAdjustmentReason.RECOUNT,
    val adjustmentNotes: String = "",
) {
    val canEdit get() = currentUser?.isAdmin == true
}

sealed interface StockManagementEvent {
    data class SearchStock(val query: String) : StockManagementEvent
    data class FilterByStore(val store: Store?) : StockManagementEvent

    data class ShowAdjustmentDialog(val stockItem: StoreStock) : StockManagementEvent
    data object DismissAdjustmentDialog : StockManagementEvent
    data class UpdateAdjustmentQuantity(val quantity: String) : StockManagementEvent
    data class UpdateAdjustmentReason(val reason: StockAdjustmentReason) : StockManagementEvent
    data class UpdateAdjustmentNotes(val notes: String) : StockManagementEvent
    data object SaveStockAdjustment : StockManagementEvent
}
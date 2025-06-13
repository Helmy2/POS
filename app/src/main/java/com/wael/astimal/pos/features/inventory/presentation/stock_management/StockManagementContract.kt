package com.wael.astimal.pos.features.inventory.presentation.stock_management

import androidx.annotation.StringRes
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.inventory.domain.entity.StoreStock


data class StockManagementState(
    val loading: Boolean = false,
    val stores: List<Store> = emptyList(),
    val stocks: List<StoreStock> = emptyList(),
    val query: String = "",
    val selectedStore: Store? = null,

    val showAdjustmentDialog: Boolean = false,
    val adjustmentTarget: StoreStock? = null,
    val adjustmentQuantityChange: String = "",
    val adjustmentReason: StockAdjustmentReason = StockAdjustmentReason.RECOUNT,
    val adjustmentNotes: String = "",

    @StringRes val error: Int? = null,
    @StringRes val snackbarMessage: Int? = null
)

sealed interface StockManagementEvent {
    data class SearchStock(val query: String) : StockManagementEvent
    data class FilterByStore(val store: Store?) : StockManagementEvent

    data class ShowAdjustmentDialog(val stockItem: StoreStock) : StockManagementEvent
    data object DismissAdjustmentDialog : StockManagementEvent
    data class UpdateAdjustmentQuantity(val quantity: String) : StockManagementEvent
    data class UpdateAdjustmentReason(val reason: StockAdjustmentReason) : StockManagementEvent
    data class UpdateAdjustmentNotes(val notes: String) : StockManagementEvent
    data object SaveStockAdjustment : StockManagementEvent

    data object ClearSnackbar : StockManagementEvent
    data object ClearError : StockManagementEvent
}
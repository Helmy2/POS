package com.wael.astimal.pos.features.inventory.presentation.stock_management

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.util.SHOULD_SHOW_SHEATH_ON_START
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustment
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.user.domain.entity.User

object StockManagementContract {

    data class State(
        val isLoading: Boolean = true,
        val stores: List<Store> = emptyList(),
        val products: List<Product> = emptyList(),
        val stockAdjustments: List<StockAdjustment> = emptyList(),
        val query: String = "",
        val currentUser: User? = null,

        val isSearchActive: Boolean = SHOULD_SHOW_SHEATH_ON_START,

        val selectedAdjustment: StockAdjustment? = null,
        val adjustmentStore: Store? = null,
        val adjustmentProduct: Product? = null,
        val adjustmentQuantityChange: String = "",
        val adjustmentReason: StockAdjustmentReason = StockAdjustmentReason.RECOUNT,
        val adjustmentNotes: String = "",
        val showDeleteDialog: Boolean = false
    ) : Reducer.ViewState {
        val canUserEdit: Boolean get() = currentUser?.isAdmin == true
        val canSave: Boolean get() = adjustmentStore != null && adjustmentProduct != null && adjustmentQuantityChange.isNotBlank()
    }

    sealed interface Event : Reducer.ViewEvent {
        // UI Actions
        data object LoadInitialData : Event
        data class SearchQueryChanged(val query: String) : Event

        // Dialog Input Changes
        data class AdjustmentQuantityChanged(val quantity: String) : Event
        data class AdjustmentReasonChanged(val reason: StockAdjustmentReason) : Event
        data class AdjustmentNotesChanged(val notes: String) : Event
        data class AdjustmentStoreChanged(val store: Store) : Event
        data class AdjustmentProductChanged(val product: Product) : Event
        data class SelectedAdjustmentChanged(val adjustment: StockAdjustment?) : Event
        data class SearchActiveChanged(val isActive: Boolean) : Event

        // Data results from ViewModel
        data class UserLoaded(val user: User?) : Event
        data class StoresLoaded(val stores: List<Store>) : Event
        data class StocksAdjustmentLoaded(val stockAdjustments: List<StockAdjustment>) : Event
        data class ProductsLoaded(val products: List<Product>) : Event

        data object AdjustmentSucceeded : Event
        data object AdjustmentFailed : Event
        data object DeleteSucceeded : Event

        data object NavigateBack : Event
        data object DeleteClicked : Event
        data object DeleteCanceled : Event
        data object DeleteConfirmed : Event
        data object SaveClicked : Event
        data object NewStockAdjustmentClicked : Event
        data object LoadingStarted : Event
        data object LoadingFinished : Event
    }
}

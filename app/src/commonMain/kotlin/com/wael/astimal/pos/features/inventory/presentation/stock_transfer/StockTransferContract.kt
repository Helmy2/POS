package com.wael.astimal.pos.features.inventory.presentation.stock_transfer

import StockTransfer
import StockTransferStatus
import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.SHOULD_SHOW_SHEATH_ON_START
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.user.domain.entity.User
import java.util.UUID

object StockTransferContract {

    // Represents the data needed to fill dropdowns
    data class DropdownData(
        val stores: List<Store> = emptyList(),
        val products: List<Product> = emptyList()
    )

    // Represents a single item being edited in the transfer form
    data class EditableStockTransferItem(
        val editorId: String = UUID.randomUUID().toString(),
        val product: Product? = null,
        val isSelectedUnitMax: Boolean = true,
        val maxUnitQuantity: String = "1.0",
        val minUnitQuantity: String = "0.0",
        val currentMaxStock: Double = 0.0
    )

    // Represents the entire transfer form being edited
    data class EditableStockTransfer(
        val fromStore: Store? = null,
        val toStore: Store? = null,
        val selectedEmployee: User? = null,
        val transferDate: Long = Clock.now(),
        val notes: String = "",
        val items: List<EditableStockTransferItem> = emptyList()
    )

    data class State(
        val isLoading: Boolean = false,
        val transfers: List<StockTransfer> = emptyList(),
        val selectedTransfer: StockTransfer? = null,
        val searchQuery: String = "",
        val isSearchActive: Boolean = SHOULD_SHOW_SHEATH_ON_START,
        val currentUser: User? = null,
        val dropdownData: DropdownData = DropdownData(),
        val currentTransferInput: EditableStockTransfer
    ) : Reducer.ViewState {
        val isEditing: Boolean get() = selectedTransfer != null
        val canUserEdit: Boolean
            get() = currentUser?.isAdmin == true &&
                    selectedTransfer?.status != StockTransferStatus.APPROVED &&
                    selectedTransfer?.status != StockTransferStatus.REJECTED
        val canSave: Boolean
            get() = canUserEdit &&
                    currentTransferInput.fromStore != null &&
                    currentTransferInput.toStore != null &&
                    currentTransferInput.items.isNotEmpty() &&
                    currentTransferInput.items.all { it.product != null }

        val canUpdateStatus: Boolean
            get() = selectedTransfer?.status ==
                    StockTransferStatus.PENDING && selectedTransfer.receivingUser == currentUser

        val havePendingTransfer: Boolean
            get() = transfers.any {
                it.status == StockTransferStatus.PENDING &&
                        it.receivingUser == currentUser
            }
    }

    sealed interface Event : Reducer.ViewEvent {
        // UI Actions
        data class SearchQueryChanged(val query: String) : Event
        data class SearchActiveChanged(val isActive: Boolean) : Event
        data class TransferSelected(val transfer: StockTransfer) : Event
        data object NewTransferClicked : Event
        data object SaveClicked : Event
        data object DeleteClicked : Event
        data object BackClicked : Event
        data object ApprovedClicked : Event
        data object RejectedClicked : Event

        // Form Input Changes
        data class FromStoreChanged(val store: Store?) : Event
        data class ToStoreChanged(val store: Store?) : Event
        data class EmployeeChanged(val employee: User?) : Event
        data class DateChanged(val date: Long) : Event
        data object AddItem : Event
        data class RemoveItem(val editorId: String) : Event
        data class ItemProductChanged(val editorId: String, val product: Product?) : Event
        data class ItemUnitChanged(val editorId: String, val isMaxUnit: Boolean) : Event
        data class ItemMaxQuantityChanged(val editorId: String, val quantity: String) : Event
        data class ItemMinQuantityChanged(val editorId: String, val quantity: String) : Event

        // Data results from ViewModel
        data class UserLoaded(val user: User?, val fromStore: Store?) : Event
        data class DropdownDataLoaded(val data: DropdownData) : Event
        data class TransfersLoaded(val transfers: List<StockTransfer>) : Event
        data class StockForItemSelected(val editorId: String, val stock: Double) : Event
        data object SaveSucceeded : Event
        data object DeleteSucceeded : Event
        data object LoadingStarted : Event
        data object LoadingFinished : Event
    }
}

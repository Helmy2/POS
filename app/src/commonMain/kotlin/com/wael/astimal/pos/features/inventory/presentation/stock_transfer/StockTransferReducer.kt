package com.wael.astimal.pos.features.inventory.presentation.stock_transfer

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.SHOULD_SHOW_SHEATH_ON_START
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransferStatus
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.user.domain.PermissionManager
import com.wael.astimal.pos.features.user.domain.entity.User
import java.util.UUID

class StockTransferReducer() :
    Reducer<StockTransferReducer.State, StockTransferReducer.Event, Nothing> {

    data class DropdownData(
        val formStores: List<Store> = emptyList(),
        val toStores: List<Store> = emptyList(),
        val products: List<Product> = emptyList()
    )

    data class EditableStockTransferItem(
        val editorId: String = UUID.randomUUID().toString(),
        val product: Product? = null,
        val isSelectedUnitMax: Boolean = true,
        val maxUnitQuantity: String = "1.0",
        val minUnitQuantity: String = "0.0",
        val currentMaxStock: Double = 0.0
    )

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

        val canUpdateStatus: Boolean
            get() = selectedTransfer?.status == StockTransferStatus.PENDING && selectedTransfer.receivingUser == currentUser

        val havePendingTransfer: Boolean
            get() = transfers.any {
                it.status == StockTransferStatus.PENDING && it.receivingUser == currentUser
            }

        val enabledFab: Boolean
            get() = currentTransferInput.fromStore != null && currentTransferInput.toStore != null && currentTransferInput.items.isNotEmpty() && currentTransferInput.items.all { it.product != null }
        val canCreate: Boolean get() = PermissionManager.canCreate(Destination.StockTransfer())
        val canUpdate: Boolean
            get() = PermissionManager.canUpdate(Destination.StockTransfer()) && selectedTransfer?.initiatingUser?.id == currentUser?.id && selectedTransfer?.status == StockTransferStatus.PENDING && enabledFab
        val canDelete: Boolean
            get() = PermissionManager.canDelete(Destination.StockTransfer()) && selectedTransfer?.initiatingUser?.id == currentUser?.id && selectedTransfer?.status != StockTransferStatus.APPROVED
        val canEdit: Boolean get() = (canCreate && !isEditing) || (canUpdate && isEditing) && selectedTransfer?.status != StockTransferStatus.APPROVED && selectedTransfer?.status != StockTransferStatus.REJECTED && (selectedTransfer?.initiatingUser?.id == currentUser?.id || selectedTransfer == null)
    }

    sealed interface Event : Reducer.ViewEvent {
        // UI Actions
        data class SearchQueryChanged(val query: String) : Event
        data class SearchActiveChanged(val isActive: Boolean) : Event
        data class TransferSelected(val transfer: StockTransfer) : Event
        data object NewTransferClicked : Event
        data object SaveClicked : Event
        data object DeleteClicked : Event
        data object ApprovedClicked : Event
        data object RejectedClicked : Event
        data object ApprovedSucceeded : Event
        data object ApprovedFauild : Event

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
        data class UserLoaded(val user: User?) : Event
        data class DropdownDataLoaded(val data: DropdownData) : Event
        data class TransfersLoaded(val transfers: List<StockTransfer>) : Event
        data class StockForItemSelected(val editorId: String, val stock: Double) : Event
        data object SaveSucceeded : Event
        data object DeleteSucceeded : Event
        data object LoadingStarted : Event
        data object LoadingFinished : Event
    }

    override fun reduce(
        previousState: State, event: Event
    ): Pair<State, Nothing?> {
        return when (event) {
            is Event.ItemProductChanged -> {
                val updatedItems = previousState.currentTransferInput.items.map { item ->
                    if (item.editorId == event.editorId) {
                        val conversionFactor = event.product?.subUnitsPerMainUnit ?: 1.0
                        item.copy(
                            product = event.product,
                            maxUnitQuantity = "1.0",
                            minUnitQuantity = conversionFactor.toString(),
                            isSelectedUnitMax = true
                        )
                    } else item
                }
                previousState.copy(
                    currentTransferInput = previousState.currentTransferInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is Event.ItemUnitChanged -> {
                val updatedItems = previousState.currentTransferInput.items.map { item ->
                    if (item.editorId == event.editorId) item.copy(isSelectedUnitMax = event.isMaxUnit) else item
                }
                previousState.copy(
                    currentTransferInput = previousState.currentTransferInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is Event.ItemMaxQuantityChanged -> {
                val updatedItems = previousState.currentTransferInput.items.map { item ->
                    if (item.editorId == event.editorId) {
                        val conversionFactor = item.product?.subUnitsPerMainUnit ?: 1.0
                        val maxQty = event.quantity.toDoubleOrNull() ?: 0.0
                        item.copy(
                            maxUnitQuantity = event.quantity,
                            minUnitQuantity = (maxQty * conversionFactor).toString()
                        )
                    } else item
                }
                previousState.copy(
                    currentTransferInput = previousState.currentTransferInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is Event.ItemMinQuantityChanged -> {
                val updatedItems = previousState.currentTransferInput.items.map { item ->
                    if (item.editorId == event.editorId) {
                        val conversionFactor = item.product?.subUnitsPerMainUnit ?: 1.0
                        val minQty = event.quantity.toDoubleOrNull() ?: 0.0
                        item.copy(
                            minUnitQuantity = event.quantity,
                            maxUnitQuantity = if (conversionFactor > 0) (minQty / conversionFactor).toString() else "0.0"
                        )
                    } else item
                }
                previousState.copy(
                    currentTransferInput = previousState.currentTransferInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            // ... other reducer cases remain the same

            is Event.LoadingStarted -> previousState.copy(isLoading = true) to null

            is Event.LoadingFinished -> previousState.copy(isLoading = false) to null

            is Event.SearchQueryChanged -> previousState.copy(searchQuery = event.query) to null

            is Event.SearchActiveChanged -> previousState.copy(isSearchActive = event.isActive) to null

            is Event.UserLoaded -> previousState.copy(
                currentUser = event.user,
                currentTransferInput = previousState.currentTransferInput.copy(
                    selectedEmployee = event.user,
                )
            ) to null

            is Event.DropdownDataLoaded -> previousState.copy(dropdownData = event.data) to null

            is Event.TransfersLoaded -> previousState.copy(
                isLoading = false,
                transfers = event.transfers
            ) to null

            is Event.TransferSelected -> {
                val editableItems = event.transfer.items.map {
                    EditableStockTransferItem(
                        editorId = it.id,
                        product = it.product,
                        maxUnitQuantity = it.quantity.toString(),
                        minUnitQuantity = (it.quantity * it.product.subUnitsPerMainUnit).toString()
                    )
                }
                previousState.copy(
                    selectedTransfer = event.transfer, currentTransferInput = EditableStockTransfer(
                        fromStore = event.transfer.fromStore,
                        toStore = event.transfer.toStore,
                        selectedEmployee = event.transfer.initiatingUser,
                        transferDate = event.transfer.createdAt,
                        items = editableItems
                    ), isSearchActive = false
                ) to null
            }

            is Event.NewTransferClicked, is Event.SaveSucceeded, is Event.DeleteSucceeded, is Event.ApprovedSucceeded -> previousState.copy(
                isLoading = false,
                selectedTransfer = null,
                currentTransferInput = EditableStockTransfer(
                    transferDate = Clock.now(),
                    selectedEmployee = previousState.currentUser,
                    fromStore = if (previousState.currentUser?.isAdmin == false) previousState.currentTransferInput.fromStore else null
                )
            ) to null

            // Form input updates
            is Event.FromStoreChanged -> previousState.copy(
                currentTransferInput = previousState.currentTransferInput.copy(
                    fromStore = event.store
                )
            ) to null

            is Event.ToStoreChanged -> previousState.copy(
                currentTransferInput = previousState.currentTransferInput.copy(
                    toStore = event.store
                )
            ) to null

            is Event.EmployeeChanged -> previousState.copy(
                currentTransferInput = previousState.currentTransferInput.copy(
                    selectedEmployee = event.employee
                )
            ) to null

            is Event.DateChanged -> previousState.copy(
                currentTransferInput = previousState.currentTransferInput.copy(
                    transferDate = event.date
                )
            ) to null

            is Event.AddItem -> {
                val newItems =
                    previousState.currentTransferInput.items + EditableStockTransferItem()
                previousState.copy(
                    currentTransferInput = previousState.currentTransferInput.copy(
                        items = newItems
                    )
                ) to null
            }

            is Event.RemoveItem -> {
                val updatedItems =
                    previousState.currentTransferInput.items.filterNot { it.editorId == event.editorId }
                previousState.copy(
                    currentTransferInput = previousState.currentTransferInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is Event.StockForItemSelected -> {
                val updatedItems = previousState.currentTransferInput.items.map { item ->
                    if (item.editorId == event.editorId) item.copy(currentMaxStock = event.stock) else item
                }
                previousState.copy(
                    currentTransferInput = previousState.currentTransferInput.copy(
                        items = updatedItems
                    )
                ) to null
            }

            is Event.SaveClicked, is Event.DeleteClicked, is Event.ApprovedClicked, is Event.RejectedClicked, is Event.ApprovedFauild -> previousState to null
        }
    }
}

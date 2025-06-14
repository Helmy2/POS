package com.wael.astimal.pos.features.inventory.presentation.stock_transfer

import androidx.annotation.StringRes
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.user.domain.entity.User
import java.util.UUID

data class StockTransferScreenState(
    val loading: Boolean = false,
    val transfers: List<StockTransfer> = emptyList(),
    val selectedTransfer: StockTransfer? = null,
    val query: String = "",
    val isQueryActive: Boolean = false,
    val currentTransferInput: EditableStockTransfer = EditableStockTransfer(),
    val availableStores: List<Store> = emptyList(),
    val availableEmployees: List<User> = emptyList(),
    val availableProducts: List<Product> = emptyList(),
    val currentUser: User? = null,
    @StringRes val error: Int? = null,
    @StringRes val snackbarMessage: Int? = null
) {
    val isNew: Boolean get() = selectedTransfer == null
    val canEditEmployee: Boolean get() = currentUser?.isAdmin == true
    val canEdit: Boolean get() = currentUser?.isAdmin == true || currentTransferInput.selectedEmployeeId == currentUser?.id
}

data class EditableStockTransfer(
    val localId: Long = 0L,
    val fromStoreId: Long? = null,
    val toStoreId: Long? = null,
    val selectedEmployeeId: Long? = null,
    val transferDate: Long? = System.currentTimeMillis(),
    val items: List<EditableStockTransferItem> = listOf(),
)

data class EditableStockTransferItem(
    val tempEditorId: String = UUID.randomUUID().toString(),
    val product: Product? = null,
    val isSelectedUnitIsMax: Boolean = true,
    val maxUnitQuantity: String = "1.0",
    val minUnitQuantity: String = "0.0",
    val currentStock: Double = 0.0
)

sealed interface StockTransferScreenEvent {
    data class SearchTransfers(val query: String) : StockTransferScreenEvent
    data class SelectTransferToView(val transfer: StockTransfer?) : StockTransferScreenEvent
    data class UpdateIsQueryActive(val isQueryActive: Boolean) : StockTransferScreenEvent
    data object OpenNewTransferForm : StockTransferScreenEvent
    data class UpdateFromStore(val store: Store?) : StockTransferScreenEvent
    data class UpdateToStore(val store: Store?) : StockTransferScreenEvent
    data object AddItemToTransfer : StockTransferScreenEvent
    data class RemoveItemFromTransfer(val itemEditorId: String) : StockTransferScreenEvent
    data class SelectEmployee(val id: Long?) : StockTransferScreenEvent
    data class UpdateItemProduct(val itemEditorId: String, val product: Product?) : StockTransferScreenEvent
    data class UpdateTransferDate(val date: Long?) : StockTransferScreenEvent
    data class UpdateItemUnit(val itemEditorId: String, val isMaxUnitSelected: Boolean) : StockTransferScreenEvent
    data class UpdateItemMaxUnitQuantity(val itemEditorId: String, val quantity: String) : StockTransferScreenEvent
    data class UpdateItemMinUnitQuantity(val itemEditorId: String, val quantity: String) : StockTransferScreenEvent
    data object SaveTransfer : StockTransferScreenEvent
    data class DeleteTransfer(val transferLocalId: Long) : StockTransferScreenEvent
    data object ClearSnackbar : StockTransferScreenEvent
    data object ClearError : StockTransferScreenEvent
}

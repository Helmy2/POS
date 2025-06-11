package com.wael.astimal.pos.features.inventory.presentation.stock_transfer

import androidx.annotation.StringRes
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.inventory.domain.entity.Unit
import com.wael.astimal.pos.features.user.domain.entity.User
import java.time.LocalDate
import kotlin.random.Random
import kotlin.time.Clock

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

    @StringRes val error: Int? = null,
    @StringRes val snackbarMessage: Int? = null
) {
    val isNew: Boolean get() = selectedTransfer == null
}

data class EditableStockTransfer(
    val localId: Long = 1,
    val fromStoreId: Long? = null,
    val toStoreId: Long? = null,
    val selectedEmployeeId: Long? = null,
    val transferDate: Long? = System.currentTimeMillis(),
    val items: MutableList<EditableStockTransferItem> = mutableListOf(),
)

data class EditableStockTransferItem(
    val tempEditorId: Long = Random.nextLong(),
    val product: Product? = null,
    val unit: Unit? = null,
    val quantity: String = "",
    val maxOpeningBalance: String = "",
    val minOpeningBalance: String = "",
)

sealed interface StockTransferScreenEvent {
    data object LoadTransfers : StockTransferScreenEvent
    data class SearchTransfers(val query: String) : StockTransferScreenEvent
    data class SelectTransferToView(val transfer: StockTransfer?) : StockTransferScreenEvent
    data class UpdateIsQueryActive(val isQueryActive: Boolean) : StockTransferScreenEvent

    data object OpenNewTransferForm : StockTransferScreenEvent
    data class UpdateFromStore(val storeId: Long?) : StockTransferScreenEvent
    data class UpdateToStore(val storeId: Long?) : StockTransferScreenEvent
    data object AddItemToTransfer : StockTransferScreenEvent
    data class RemoveItemFromTransfer(val itemEditorId: Long) : StockTransferScreenEvent
    data class SelectEmployee(val id: Long?) : StockTransferScreenEvent
    data class UpdateItemProduct(val itemEditorId: Long, val product: Product?) :
        StockTransferScreenEvent
    data class UpdateTransferDate(val date: Long?) : StockTransferScreenEvent

    data class UpdateItemUnit(val itemEditorId: Long, val unit: Unit?) : StockTransferScreenEvent

    data class UpdateItemQuantity(val itemEditorId: Long, val quantity: String) :
        StockTransferScreenEvent

    data object SaveTransfer : StockTransferScreenEvent

    data class DeleteTransfer(val transferLocalId: Long) : StockTransferScreenEvent
    data object ClearSnackbar : StockTransferScreenEvent
    data object ClearError : StockTransferScreenEvent
}
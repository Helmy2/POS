package com.wael.astimal.pos.features.inventory.presentation.stock_transfer

import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.inventory.domain.entity.Unit

data class StockTransferScreenState(
    val loading: Boolean = false,
    val transfers: List<StockTransfer> = emptyList(),
    val selectedTransfer: StockTransfer? = null,

    val isQueryActive: Boolean = false,
    val isDetailViewOpen: Boolean = false,
    val currentTransferInput: EditableStockTransfer = EditableStockTransfer(),
    val availableStores: List<Store> = emptyList(),
    val availableProducts: List<Product> = emptyList(),

    val query: String = "",
    val error: String? = null,
    val snackbarMessage: String? = null
)

data class EditableStockTransfer(
    val localId: Long = 1,
    var fromStoreId: Long? = null,
    var toStoreId: Long? = null,
    val items: MutableList<EditableStockTransferItem> = mutableListOf(),
)

data class EditableStockTransferItem(
    val tempEditorId: Long = 1,
    var product: Product? = null,
    var unit: Unit? = null,
    var quantity: String = "",
    var maxOpeningBalance: String = "",
    var minOpeningBalance: String = "",
)

sealed interface StockTransferScreenEvent {
    data object LoadTransfers : StockTransferScreenEvent
    data class SearchTransfers(val query: String) : StockTransferScreenEvent
    data class SelectTransferToView(val transfer: StockTransfer?) : StockTransferScreenEvent
    data class UpdateIsQueryActive(val isQueryActive: Boolean) : StockTransferScreenEvent


    data object OpenNewTransferForm : StockTransferScreenEvent
    data object CloseTransferForm : StockTransferScreenEvent
    data class UpdateFromStore(val storeId: Long?) : StockTransferScreenEvent
    data class UpdateToStore(val storeId: Long?) : StockTransferScreenEvent
    data object AddItemToTransfer : StockTransferScreenEvent
    data class RemoveItemFromTransfer(val itemEditorId: Long) : StockTransferScreenEvent
    data class UpdateItemProduct(val itemEditorId: Long, val product: Product?) :
        StockTransferScreenEvent

    data class UpdateItemUnit(val itemEditorId: Long, val unit: Unit?) : StockTransferScreenEvent

    data class UpdateItemQuantity(val itemEditorId: Long, val quantity: String) :
        StockTransferScreenEvent

    data object SaveTransfer : StockTransferScreenEvent

    data class DeleteTransfer(val transferLocalId: Long) : StockTransferScreenEvent
    data object ClearSnackbar : StockTransferScreenEvent
}
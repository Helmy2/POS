package com.wael.astimal.pos.features.management.presentaion.purchase_return

import androidx.annotation.StringRes
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.PurchaseReturn
import com.wael.astimal.pos.features.management.domain.entity.Supplier
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.user.domain.entity.User
import java.util.UUID

data class PurchaseReturnScreenState(
    val loading: Boolean = false,
    val returns: List<PurchaseReturn> = emptyList(),
    val selectedReturn: PurchaseReturn? = null,
    val newReturnInput: EditablePurchaseReturn = EditablePurchaseReturn(),
    val availableSuppliers: List<Supplier> = emptyList(),
    val availableProducts: List<Product> = emptyList(),
    val availableEmployees: List<User> = emptyList(),
    val availableUnits: List<Unit> = emptyList(),
    val query: String = "",
    @StringRes val error: Int? = null,
    @StringRes val snackbarMessage: Int? = null,
    val isQueryActive: Boolean = false
) {
    val isNew: Boolean get() = selectedReturn == null
}

data class EditablePurchaseReturn(
    val selectedSupplier: Supplier? = null,
    val selectedEmployeeId: Long? = null,
    val paymentType: PaymentType = PaymentType.CASH,
    val items: List<EditablePurchaseReturnItem> = listOf(),
    val totalReturnedValue: Double = 0.0
)

data class EditablePurchaseReturnItem(
    val tempEditorId: String = UUID.randomUUID().toString(),
    val product: Product? = null,
    val selectedUnit: com.wael.astimal.pos.features.inventory.domain.entity.Unit? = null,
    val quantity: String = "1",
    val purchasePrice: String = "0.0",
    val lineTotal: Double = 0.0
)

sealed interface PurchaseReturnScreenEvent {
    data class SearchReturns(val query: String) : PurchaseReturnScreenEvent
    data class SelectReturnToView(val purchaseReturn: PurchaseReturn?) : PurchaseReturnScreenEvent
    data class UpdateQuery(val query: String) : PurchaseReturnScreenEvent
    data class UpdateIsQueryActive(val isActive: Boolean) : PurchaseReturnScreenEvent
    data object OpenNewReturnForm : PurchaseReturnScreenEvent
    data class SelectSupplier(val supplier: Supplier?) : PurchaseReturnScreenEvent
    data class SelectEmployee(val id: Long?) : PurchaseReturnScreenEvent
    data class UpdatePaymentType(val type: PaymentType) : PurchaseReturnScreenEvent
    data object AddItemToReturn : PurchaseReturnScreenEvent
    data class RemoveItemFromReturn(val tempEditorId: String) : PurchaseReturnScreenEvent
    data class UpdateItemProduct(val tempEditorId: String, val product: Product?) :
        PurchaseReturnScreenEvent

    data class UpdateItemUnit(val tempEditorId: String, val unit: com.wael.astimal.pos.features.inventory.domain.entity.Unit?) : PurchaseReturnScreenEvent
    data class UpdateItemQuantity(val tempEditorId: String, val quantity: String) :
        PurchaseReturnScreenEvent

    data class UpdateItemPrice(val tempEditorId: String, val price: String) :
        PurchaseReturnScreenEvent

    data object SaveReturn : PurchaseReturnScreenEvent
    data object DeleteReturn : PurchaseReturnScreenEvent
    data object ClearSnackbar : PurchaseReturnScreenEvent
    data object ClearError : PurchaseReturnScreenEvent
}
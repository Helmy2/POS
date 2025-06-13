package com.wael.astimal.pos.features.management.presentation.purchase_return

import androidx.annotation.StringRes
import com.wael.astimal.pos.features.management.domain.entity.EditableItemList
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.PurchaseReturn
import com.wael.astimal.pos.features.management.domain.entity.Supplier
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.user.domain.entity.User

data class PurchaseReturnScreenState(
    val loading: Boolean = false,
    val returns: List<PurchaseReturn> = emptyList(),
    val selectedReturn: PurchaseReturn? = null,
    val selectedSupplier: Supplier? = null,
    val input: EditableItemList = EditableItemList(),
    val availableSuppliers: List<Supplier> = emptyList(),
    val availableProducts: List<Product> = emptyList(),
    val availableEmployees: List<User> = emptyList(),
    val availableUnits: List<Unit> = emptyList(),
    val query: String = "",
    @StringRes val error: Int? = null,
    @StringRes val snackbarMessage: Int? = null,
    val isQueryActive: Boolean = false,
    val currentUser: User? = null,
) {
    val isNew: Boolean get() = selectedReturn == null
    val canEdit: Boolean
        get() = currentUser?.isAdmin == true ||
            (input.selectedEmployeeId == currentUser?.id && currentUser?.isEmployee == true)
}

sealed interface PurchaseReturnScreenEvent {
    data class SearchReturns(val query: String) : PurchaseReturnScreenEvent
    data class SelectReturnToView(val purchaseReturn: PurchaseReturn?) : PurchaseReturnScreenEvent
    data class UpdateQuery(val query: String) : PurchaseReturnScreenEvent
    data class UpdateIsQueryActive(val isActive: Boolean) : PurchaseReturnScreenEvent
    data object OpenNewReturnForm : PurchaseReturnScreenEvent
    data class SelectSupplier(val supplier: Supplier?) : PurchaseReturnScreenEvent
    data class SelectEmployee(val id: Long?) : PurchaseReturnScreenEvent
    data class UpdatePaymentType(val type: PaymentType?) : PurchaseReturnScreenEvent
    data object AddItemToReturn : PurchaseReturnScreenEvent
    data class RemoveItemFromReturn(val tempEditorId: String) : PurchaseReturnScreenEvent
    data class UpdateItemProduct(val tempEditorId: String, val product: Product?) : PurchaseReturnScreenEvent
    data class UpdateItemUnit(val tempEditorId: String, val productUnit: com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit?) : PurchaseReturnScreenEvent
    data class UpdateItemQuantity(val tempEditorId: String, val quantity: String) : PurchaseReturnScreenEvent
    data class UpdateItemPrice(val tempEditorId: String, val price: String) : PurchaseReturnScreenEvent
    data class UpdateAmountPaid(val amountPaid: String) : PurchaseReturnScreenEvent
    data class UpdateTransferDate(val date: Long?) : PurchaseReturnScreenEvent
    data object SaveReturn : PurchaseReturnScreenEvent
    data object DeleteReturn : PurchaseReturnScreenEvent
    data object ClearSnackbar : PurchaseReturnScreenEvent
    data object ClearError : PurchaseReturnScreenEvent
}

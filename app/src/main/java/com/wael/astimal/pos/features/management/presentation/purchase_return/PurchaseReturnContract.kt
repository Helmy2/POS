package com.wael.astimal.pos.features.management.presentation.purchase_return

import androidx.annotation.StringRes
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.management.domain.entity.EditableItemList
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.PurchaseReturn
import com.wael.astimal.pos.features.management.domain.entity.Supplier
import com.wael.astimal.pos.features.user.domain.entity.User

data class PurchaseReturnState(
    val loading: Boolean = false,
    var currentUser: User? = null,
    val returns: List<PurchaseReturn> = emptyList(),
    val selectedReturn: PurchaseReturn? = null,
    val selectedSupplier: Supplier? = null,
    val currentReturnInput: EditableItemList = EditableItemList(),
    val availableSuppliers: List<Supplier> = emptyList(),
    val availableProducts: List<Product> = emptyList(),
    val availableEmployees: List<User> = emptyList(),
    val query: String = "",
    @StringRes val error: Int? = null,
    @StringRes val snackbarMessage: Int? = null,
    val isQueryActive: Boolean = false,
) {
    val isNew: Boolean get() = selectedReturn == null
    val canEdit: Boolean
        get() {
            return currentUser?.isAdmin == true || (currentReturnInput.selectedEmployeeId == currentUser?.id && currentUser?.isEmployee == true)
        }
}

sealed interface PurchaseReturnEvent {
    data class SearchReturns(val query: String) : PurchaseReturnEvent
    data class SelectReturnToView(val purchaseReturn: PurchaseReturn?) : PurchaseReturnEvent
    data class UpdateIsQueryActive(val isActive: Boolean) : PurchaseReturnEvent
    data class UpdateQuery(val query: String) : PurchaseReturnEvent
    data class SelectSupplier(val supplier: Supplier?) : PurchaseReturnEvent
    data class SelectEmployee(val employeeId: Long?) : PurchaseReturnEvent
    data object DeleteReturn : PurchaseReturnEvent
    data object AddItemToReturn : PurchaseReturnEvent
    data class RemoveItemFromReturn(val tempEditorId: String) : PurchaseReturnEvent
    data class UpdatePaymentType(val type: PaymentType?) : PurchaseReturnEvent
    data class UpdateReturnDate(val date: Long?) : PurchaseReturnEvent
    data class UpdateAmountPaid(val amount: String) : PurchaseReturnEvent
    data class UpdateItemProduct(val tempEditorId: String, val product: Product?) : PurchaseReturnEvent
    data class UpdateItemUnit(val tempEditorId: String, val isMaxUnitSelected: Boolean) : PurchaseReturnEvent
    data class UpdateItemMaxUnitQuantity(val tempEditorId: String, val quantity: String) : PurchaseReturnEvent
    data class UpdateItemMinUnitQuantity(val tempEditorId: String, val quantity: String) : PurchaseReturnEvent
    data class UpdateItemMaxUnitPrice(val tempEditorId: String, val price: String) : PurchaseReturnEvent
    data class UpdateItemMinUnitPrice(val tempEditorId: String, val price: String) : PurchaseReturnEvent
    data object SaveReturn : PurchaseReturnEvent
    data object ClearSnackbar : PurchaseReturnEvent
    data object ClearError : PurchaseReturnEvent
    data object OpenNewReturnForm : PurchaseReturnEvent
}

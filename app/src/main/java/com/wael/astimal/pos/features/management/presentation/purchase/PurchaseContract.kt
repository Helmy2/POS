package com.wael.astimal.pos.features.management.presentation.purchase

import androidx.annotation.StringRes
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.management.domain.entity.EditableItemList
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.PurchaseOrder
import com.wael.astimal.pos.features.management.domain.entity.Supplier
import com.wael.astimal.pos.features.user.domain.entity.User

data class PurchaseState(
    val loading: Boolean = false,
    var currentUser: User? = null,
    val purchases: List<PurchaseOrder> = emptyList(),
    val selectedPurchase: PurchaseOrder? = null,
    val selectedSupplier: Supplier? = null,
    val currentPurchaseInput: EditableItemList = EditableItemList(),
    val availableSuppliers: List<Supplier> = emptyList(),
    val availableProducts: List<Product> = emptyList(),
    val availableEmployees: List<User> = emptyList(),
    val query: String = "",
    @StringRes val error: Int? = null,
    @StringRes val snackbarMessage: Int? = null,
    val isQueryActive: Boolean = false,
) {
    val isNew: Boolean get() = selectedPurchase == null
    val canEdit: Boolean
        get() {
            return currentUser?.isAdmin == true || (currentPurchaseInput.selectedEmployeeId == currentUser?.id && currentUser?.isEmployee == true)
        }
}

sealed interface PurchaseEvent {
    data class SearchPurchases(val query: String) : PurchaseEvent
    data class SelectPurchaseToView(val purchase: PurchaseOrder?) : PurchaseEvent
    data class UpdateIsQueryActive(val isActive: Boolean) : PurchaseEvent
    data class UpdateQuery(val query: String) : PurchaseEvent
    data class SelectSupplier(val supplier: Supplier?) : PurchaseEvent
    data class SelectEmployee(val employeeId: Long?) : PurchaseEvent
    data object DeletePurchase : PurchaseEvent
    data object AddItemToPurchase : PurchaseEvent
    data class RemoveItemFromPurchase(val tempEditorId: String) : PurchaseEvent
    data class UpdatePaymentType(val type: PaymentType?) : PurchaseEvent
    data class UpdatePurchaseDate(val date: Long?) : PurchaseEvent
    data class UpdateAmountPaid(val amount: String) : PurchaseEvent
    data class UpdateItemProduct(val tempEditorId: String, val product: Product?) : PurchaseEvent
    data class UpdateItemUnit(val tempEditorId: String, val isMaxUnitSelected: Boolean) : PurchaseEvent
    data class UpdateItemMaxUnitQuantity(val tempEditorId: String, val quantity: String) : PurchaseEvent
    data class UpdateItemMinUnitQuantity(val tempEditorId: String, val quantity: String) : PurchaseEvent
    data class UpdateItemMaxUnitPrice(val tempEditorId: String, val price: String) : PurchaseEvent
    data class UpdateItemMinUnitPrice(val tempEditorId: String, val price: String) : PurchaseEvent
    data object SavePurchase : PurchaseEvent
    data object ClearSnackbar : PurchaseEvent
    data object ClearError : PurchaseEvent
    data object OpenNewPurchaseForm : PurchaseEvent
}

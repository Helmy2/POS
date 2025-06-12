package com.wael.astimal.pos.features.management.presentaion.purchase

import androidx.annotation.StringRes
import com.wael.astimal.pos.core.presentation.compoenents.EditableItemList
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.PurchaseOrder
import com.wael.astimal.pos.features.management.domain.entity.Supplier
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.user.domain.entity.User

data class PurchaseScreenState(
    val loading: Boolean = false,
    val currentUser: User? = null,
    val purchases: List<PurchaseOrder> = emptyList(),
    val selectedPurchase: PurchaseOrder? = null,
    val selectedSupplier: Supplier? = null,
    val currentPurchaseInput: EditableItemList = EditableItemList(),
    val availableSuppliers: List<Supplier> = emptyList(),
    val availableEmployees: List<User> = emptyList(),
    val availableProducts: List<Product> = emptyList(),
    val availableUnits: List<Unit> = emptyList(),
    val query: String = "",
    @StringRes val error: Int? = null,
    @StringRes val snackbarMessage: Int? = null,
    val isQueryActive: Boolean = false
) {
    val isNew: Boolean get() = selectedPurchase == null
    val canEdit: Boolean
        get() = currentUser?.isAdmin == true ||
            (currentPurchaseInput.selectedEmployeeId == currentUser?.id && currentUser?.isEmployee == true)
}

sealed interface PurchaseScreenEvent {
    data class SearchPurchases(val query: String) : PurchaseScreenEvent
    data class SelectPurchaseToView(val purchase: PurchaseOrder?) : PurchaseScreenEvent
    data class UpdateQuery(val query: String) : PurchaseScreenEvent
    data class UpdateIsQueryActive(val isActive: Boolean) : PurchaseScreenEvent
    data object OpenNewPurchaseForm : PurchaseScreenEvent
    data class SelectSupplier(val supplier: Supplier?) : PurchaseScreenEvent
    data class SelectEmployee(val employeeId: Long?) : PurchaseScreenEvent
    data class UpdatePaymentType(val type: PaymentType?) : PurchaseScreenEvent
    data object AddItemToPurchase : PurchaseScreenEvent
    data class RemoveItemFromPurchase(val tempEditorId: String) : PurchaseScreenEvent
    data class UpdateItemProduct(val tempEditorId: String, val product: Product?) : PurchaseScreenEvent
    data class UpdateItemUnit(val tempEditorId: String, val productUnit: com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit?) : PurchaseScreenEvent
    data class UpdateItemQuantity(val tempEditorId: String, val quantity: String) : PurchaseScreenEvent
    data class UpdateItemPrice(val tempEditorId: String, val price: String) : PurchaseScreenEvent
    data class UpdateAmountPaid(val amountPaid: String) : PurchaseScreenEvent
    data class UpdateTransferDate(val date: Long?) : PurchaseScreenEvent
    data object SavePurchase : PurchaseScreenEvent
    data object DeletePurchase : PurchaseScreenEvent
    data object ClearSnackbar : PurchaseScreenEvent
    data object ClearError : PurchaseScreenEvent
}
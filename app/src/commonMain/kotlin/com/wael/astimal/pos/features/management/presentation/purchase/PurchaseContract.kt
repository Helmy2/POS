package com.wael.astimal.pos.features.management.presentation.purchase

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.EditableItem
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.PurchaseOrder
import com.wael.astimal.pos.features.user.domain.entity.User

object PurchaseContract {

    data class DropdownData(
        val suppliers: List<BusinessPartner> = emptyList(),
        val products: List<Product> = emptyList(),
        val employees: List<User> = emptyList()
    )

    data class EditablePurchase(
        val selectedEmployee: User? = null,
        val paymentType: PaymentType = PaymentType.CASH,
        val date: Long,
        val items: List<EditableItem> = emptyList(),
        val amountPaid: String = "0.0",
        val partnerBalance: Double = 0.0
    ) {
        val totalAmount: Double get() = items.sumOf { it.lineTotal }
        val amountRemaining: Double get() = totalAmount - (amountPaid.toDoubleOrNull() ?: 0.0)
        val partnerBalanceAfterThisOrder: Double get() = partnerBalance + amountRemaining
    }

    data class State(
        val isLoading: Boolean = false,
        val purchases: List<PurchaseOrder> = emptyList(),
        val selectedPurchase: PurchaseOrder? = null,
        val selectedSupplier: BusinessPartner? = null,
        val searchQuery: String = "",
        val isSearchActive: Boolean = false,
        val currentUser: User? = null,
        val dropdownData: DropdownData = DropdownData(),
        val currentPurchaseInput: EditablePurchase
    ) : Reducer.ViewState {
        val isEditing: Boolean get() = selectedPurchase != null
        val canSave: Boolean
            get() = selectedSupplier != null &&
                    currentPurchaseInput.items.isNotEmpty() &&
                    currentPurchaseInput.items.all { it.product != null }
    }

    sealed interface Event : Reducer.ViewEvent {
        // UI Actions
        data class SearchQueryChanged(val query: String) : Event
        data class SearchActiveChanged(val isActive: Boolean) : Event
        data class PurchaseSelected(val purchase: PurchaseOrder) : Event
        data object NewPurchaseClicked : Event
        data object SaveClicked : Event
        data object DeleteClicked : Event
        data object BackClicked : Event

        // Form Input Changes
        data class SupplierSelected(val supplier: BusinessPartner?) : Event
        data class EmployeeChanged(val employee: User?) : Event
        data class DateChanged(val date: Long) : Event
        data class PaymentTypeChanged(val type: PaymentType) : Event
        data class AmountPaidChanged(val amount: String) : Event
        data object AddItem : Event
        data class RemoveItem(val editorId: String) : Event
        data class ItemProductChanged(val editorId: String, val product: Product?) : Event
        data class ItemUnitChanged(val editorId: String, val isMaxUnit: Boolean) : Event
        data class ItemMaxQuantityChanged(val editorId: String, val quantity: String) : Event
        data class ItemMinQuantityChanged(val editorId: String, val quantity: String) : Event
        data class ItemMaxPriceChanged(val editorId: String, val price: String) : Event
        data class ItemMinPriceChanged(val editorId: String, val price: String) : Event
        data class ItemStockChanged(val editorId: String, val stock: Double) : Event
        data class PartnerBalanceChanged(val balance: Double) : Event

        // Data results from ViewModel
        data class UserLoaded(val user: User?) : Event
        data class DropdownDataLoaded(val data: DropdownData) : Event
        data class PurchasesLoaded(val purchases: List<PurchaseOrder>) : Event
        data object SaveSucceeded : Event
        data object DeleteSucceeded : Event
        data object LoadingStarted : Event
        data object LoadingFinished : Event
    }
}

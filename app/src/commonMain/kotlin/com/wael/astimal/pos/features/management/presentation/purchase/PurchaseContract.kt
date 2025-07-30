package com.wael.astimal.pos.features.management.presentation.purchase

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.management.data.local.entity.PaymentMethod
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.EditableItem
import com.wael.astimal.pos.features.management.domain.entity.Invoice
import com.wael.astimal.pos.features.user.domain.entity.User

object PurchaseContract {

    data class DropdownData(
        val partners: List<BusinessPartner> = emptyList(),
        val products: List<Product> = emptyList(),
        val stores: List<Store> = emptyList()
    )

    data class EditableOrder(
        val paymentType: PaymentMethod = PaymentMethod.CASH,
        val selectedPartner: BusinessPartner? = null,
        val selectedStore: Store? = null,
        val date: Long,
        val items: List<EditableItem> = emptyList(),
        val amountPaid: String = "0.0",
        val partnerBalance: Double = 0.0
    ) {
        val totalAmount: Double get() = items.sumOf { it.lineTotal }
        val amountRemaining: Double get() = totalAmount + (amountPaid.toDoubleOrNull() ?: 0.0)
        val partnerBalanceAfterThisOrder: Double get() = partnerBalance - amountRemaining
    }

    data class State(
        val isLoading: Boolean = false,
        val orders: List<Invoice> = emptyList(),
        val selectedOrder: Invoice? = null,
        val searchQuery: String = "",
        val isSearchActive: Boolean = false,
        val currentUser: User? = null,
        val dropdownData: DropdownData = DropdownData(),
        val currentOrderInput: EditableOrder
    ) : Reducer.ViewState {
        val isEditing: Boolean get() = selectedOrder != null
        val canSave: Boolean
            get() = currentOrderInput.selectedPartner != null &&
                    currentOrderInput.selectedStore != null &&
                    currentOrderInput.items.isNotEmpty() &&
                    currentOrderInput.items.all { it.product != null }
    }

    sealed interface Event : Reducer.ViewEvent {
        // UI Actions
        data class SearchQueryChanged(val query: String) : Event
        data class SearchActiveChanged(val isActive: Boolean) : Event
        data class OrderSelected(val order: Invoice) : Event
        data object NewOrderClicked : Event
        data object SaveClicked : Event
        data object DeleteClicked : Event
        data object BackClicked : Event

        // Form Input Changes
        data class PartnerSelected(val partner: BusinessPartner?) : Event
        data class StoreChanged(val store: Store?) : Event
        data class DateChanged(val date: Long) : Event
        data class PaymentMethodChanged(val type: PaymentMethod) : Event
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
        data class LoadInitialInvoice(val id: String?) : Event


        // Data results from ViewModel
        data class UserLoaded(val user: User?) : Event
        data class DropdownDataLoaded(val data: DropdownData) : Event
        data class OrdersLoaded(val orders: List<Invoice>) : Event
        data object SaveSucceeded : Event
        data object DeleteSucceeded : Event
        data object LoadingStarted : Event
        data object LoadingFinished : Event
    }
}

package com.wael.astimal.pos.features.management.presentation.sales_return

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.EditableItem
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.SalesReturn
import com.wael.astimal.pos.features.user.domain.entity.User

object SalesReturnContract {

    data class DropdownData(
        val clients: List<BusinessPartner> = emptyList(),
        val products: List<Product> = emptyList(),
        val employees: List<User> = emptyList()
    )

    data class EditableReturn(
        val selectedEmployee: User? = null,
        val paymentType: PaymentType = PaymentType.CASH,
        val createdAt: Long,
        val items: List<EditableItem> = emptyList(),
        val amountPaid: String = "0.0",
        val partnerBalance: Double = 0.0
    ) {
        val totalAmount: Double get() = items.sumOf { it.lineTotal }
        val amountRemaining: Double get() = totalAmount - (amountPaid.toDoubleOrNull() ?: 0.0)
        val partnerBalanceAfterThisOrder: Double get() = partnerBalance - amountRemaining
    }

    data class State(
        val isLoading: Boolean = false,
        val returns: List<SalesReturn> = emptyList(),
        val selectedReturn: SalesReturn? = null,
        val selectedClient: BusinessPartner? = null,
        val searchQuery: String = "",
        val isSearchActive: Boolean = false,
        val currentUser: User? = null,
        val dropdownData: DropdownData = DropdownData(),
        val currentReturnInput: EditableReturn
    ) : Reducer.ViewState {
        val isEditing: Boolean get() = selectedReturn != null
        val canSave: Boolean
            get() = selectedClient != null &&
                    currentReturnInput.items.isNotEmpty() &&
                    currentReturnInput.items.all { it.product != null }
    }

    sealed interface Event : Reducer.ViewEvent {
        // UI Actions
        data class SearchQueryChanged(val query: String) : Event
        data class SearchActiveChanged(val isActive: Boolean) : Event
        data class ReturnSelected(val salesReturn: SalesReturn) : Event
        data object NewReturnClicked : Event
        data object SaveClicked : Event
        data object DeleteClicked : Event
        data object BackClicked : Event

        // Form Input Changes
        data class ClientSelected(val client: BusinessPartner?) : Event
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
        data class PartnerBalanceChanged(val balance: Double) : Event

        // Data results from ViewModel
        data class UserLoaded(val user: User?) : Event
        data class DropdownDataLoaded(val data: DropdownData) : Event
        data class ReturnsLoaded(val returns: List<SalesReturn>) : Event
        data object SaveSucceeded : Event
        data object DeleteSucceeded : Event
        data object LoadingStarted : Event
        data object LoadingFinished : Event
    }
}

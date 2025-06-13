package com.wael.astimal.pos.features.management.presentaion.sales_return

import androidx.annotation.StringRes
import com.wael.astimal.pos.features.management.domain.entity.EditableItemList
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import com.wael.astimal.pos.features.management.domain.entity.Client
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.SalesReturn
import com.wael.astimal.pos.features.user.domain.entity.User


data class SalesReturnState(
    val loading: Boolean = false,
    var currentUser: User? = null,

    val returns: List<SalesReturn> = emptyList(),
    val selectedReturn: SalesReturn? = null,
    val selectedClient: Client? = null,

    val currentReturnInput: EditableItemList = EditableItemList(),
    val availableClients: List<Client> = emptyList(),
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

sealed interface SalesReturnEvent {
    data class SearchReturns(val query: String) : SalesReturnEvent
    data class SelectReturnToView(val salesReturn: SalesReturn?) : SalesReturnEvent
    data class UpdateIsQueryActive(val isActive: Boolean) : SalesReturnEvent
    data class UpdateQuery(val query: String) : SalesReturnEvent

    data class SelectClient(val client: Client?) : SalesReturnEvent
    data class SelectEmployee(val employeeId: Long?) : SalesReturnEvent

    data object DeleteReturn : SalesReturnEvent
    data object AddItemToReturn : SalesReturnEvent
    data class RemoveItemFromReturn(val tempEditorId: String) : SalesReturnEvent

    data class UpdatePaymentType(val type: PaymentType?) : SalesReturnEvent
    data class UpdateReturnDate(val date: Long?) : SalesReturnEvent
    data class UpdateAmountPaid(val amount: String) : SalesReturnEvent
    data class UpdateItemProduct(val tempEditorId: String, val product: Product?) : SalesReturnEvent
    data class UpdateItemUnit(val tempEditorId: String, val productUnit: ProductUnit?) : SalesReturnEvent
    data class UpdateItemQuantity(val tempEditorId: String, val quantity: String) : SalesReturnEvent
    data class UpdateItemPrice(val tempEditorId: String, val price: String) : SalesReturnEvent

    data object SaveReturn : SalesReturnEvent
    data object ClearSnackbar : SalesReturnEvent
    data object ClearError : SalesReturnEvent
    data object OpenNewReturnForm : SalesReturnEvent
}
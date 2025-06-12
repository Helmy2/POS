package com.wael.astimal.pos.features.management.presentaion.sales_return

import androidx.annotation.StringRes
import com.wael.astimal.pos.core.presentation.compoenents.EditableItemList
import com.wael.astimal.pos.features.management.domain.entity.Client
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.SalesReturn
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import com.wael.astimal.pos.features.user.domain.entity.User

data class SalesReturnScreenState(
    val loading: Boolean = false,
    val returns: List<SalesReturn> = emptyList(),
    val selectedReturn: SalesReturn? = null,
    val input: EditableItemList = EditableItemList(),
    val availableClients: List<Client> = emptyList(),
    val availableProducts: List<Product> = emptyList(),
    val availableEmployees: List<User> = emptyList(),
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

sealed interface SalesReturnScreenEvent {
    data class SearchReturns(val query: String) : SalesReturnScreenEvent
    data class SelectReturnToView(val salesReturn: SalesReturn?) : SalesReturnScreenEvent
    data class UpdateQuery(val query: String) : SalesReturnScreenEvent
    data class UpdateSelectEmployee(val id: Long?) : SalesReturnScreenEvent
    data class UpdateIsQueryActive(val isActive: Boolean) : SalesReturnScreenEvent

    data class UpdateItemDate(val date: Long?) : SalesReturnScreenEvent

    data object OpenNewReturnForm : SalesReturnScreenEvent
    data class SelectClient(val client: Client?) : SalesReturnScreenEvent
    data class UpdatePaymentType(val type: PaymentType?) : SalesReturnScreenEvent
    data class UpdateAmountRefunded(val amount: String) : SalesReturnScreenEvent
    data object AddItemToReturn : SalesReturnScreenEvent
    data class RemoveItemFromReturn(val tempEditorId: String) : SalesReturnScreenEvent
    data class UpdateItemProduct(val tempEditorId: String, val product: Product?) : SalesReturnScreenEvent
    data class UpdateItemUnit(val tempEditorId: String, val productUnit: ProductUnit?) : SalesReturnScreenEvent
    data class UpdateItemQuantity(val tempEditorId: String, val quantity: String) : SalesReturnScreenEvent
    data class UpdateItemPrice(val tempEditorId: String, val price: String) : SalesReturnScreenEvent

    data object DeleteReturn : SalesReturnScreenEvent
    data object SaveReturn : SalesReturnScreenEvent
    data object ClearSnackbar : SalesReturnScreenEvent
    data object ClearError : SalesReturnScreenEvent
}
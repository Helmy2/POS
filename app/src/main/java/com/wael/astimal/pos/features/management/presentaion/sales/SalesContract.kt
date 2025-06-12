package com.wael.astimal.pos.features.management.presentaion.sales

import androidx.annotation.StringRes
import com.wael.astimal.pos.core.presentation.compoenents.EditableItemList
import com.wael.astimal.pos.features.management.domain.entity.Client
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.SalesOrder
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import com.wael.astimal.pos.features.user.domain.entity.User

data class OrderState(
    val loading: Boolean = false,
    var currentUser: User? = null,

    val orders: List<SalesOrder> = emptyList(),
    val selectedOrder: SalesOrder? = null,

    val selectedClient: Client? = null,
    val currentOrderInput: EditableItemList = EditableItemList(),
    val availableClients: List<Client> = emptyList(),
    val availableProducts: List<Product> = emptyList(),
    val availableEmployees: List<User> = emptyList(),

    val query: String = "",
    @StringRes val error: Int? = null,
    @StringRes val snackbarMessage: Int? = null,

    val isQueryActive: Boolean = false,
) {
    val isNew: Boolean get() = selectedOrder == null
    val canEdit: Boolean
        get() {
            return currentUser?.isAdmin == true || (currentOrderInput.selectedEmployeeId == currentUser?.id && currentUser?.isEmployee == true)
        }
}


sealed interface OrderEvent {
    data class SearchOrders(val query: String) : OrderEvent
    data class SelectOrderToView(val order: SalesOrder?) : OrderEvent
    data class UpdateIsQueryActive(val isActive: Boolean) : OrderEvent
    data class UpdateQuery(val query: String) : OrderEvent

    data class SelectClient(val client: Client?) : OrderEvent
    data class SelectEmployee(val employeeId: Long?) : OrderEvent

    data class DeleteOrder(val localId: Long) : OrderEvent
    data object AddItemToOrder : OrderEvent
    data class RemoveItemFromOrder(val tempEditorId: String) : OrderEvent

    data class UpdatePaymentType(val type: PaymentType?) : OrderEvent
    data class UpdateTransferDate(val date: Long?) : OrderEvent
    data class UpdateAmountPaid(val amount: String) : OrderEvent
    data class UpdateItemProduct(val tempEditorId: String, val product: Product?) : OrderEvent
    data class UpdateItemUnit(val tempEditorId: String, val productUnit: ProductUnit?) : OrderEvent
    data class UpdateItemQuantity(val tempEditorId: String, val quantity: String) : OrderEvent
    data class UpdateItemPrice(val tempEditorId: String, val price: String) : OrderEvent

    data object SaveOrder : OrderEvent
    data object ClearSnackbar : OrderEvent
    data object ClearError : OrderEvent
    data object OpenNewOrderForm : OrderEvent
}
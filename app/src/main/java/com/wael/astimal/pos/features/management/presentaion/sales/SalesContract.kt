package com.wael.astimal.pos.features.management.presentaion.sales

import androidx.annotation.StringRes
import com.wael.astimal.pos.features.management.domain.entity.Client
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.SalesOrder
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.Unit
import com.wael.astimal.pos.features.user.domain.entity.User
import java.util.UUID

data class OrderState(
    val loading: Boolean = false,

    val orders: List<SalesOrder> = emptyList(),
    val selectedOrder: SalesOrder? = null,

    val currentOrderInput: EditableOrder = EditableOrder(),
    val availableClients: List<Client> = emptyList(),
    val availableProducts: List<Product> = emptyList(),
    val availableEmployees: List<User> = emptyList(),

    val query: String = "",
    @StringRes val error: Int? = null,
    @StringRes val snackbarMessage: Int? = null,

    val isQueryActive: Boolean = false,
){
    val isNew: Boolean get() = selectedOrder == null
}


data class EditableOrder(
    val selectedClient: Client? = null,
    val selectedEmployeeId: Long? = null,
    val selectedMainEmployeeId: Long? = null,
    val paymentType: PaymentType = PaymentType.CASH,
    val items: List<EditableOrderItem> = listOf(),
    val amountPaid: String = "0.0",
    val subtotal: Double = 0.0,
    val totalGain: Double = 0.0,
    val totalAmount: Double = 0.0,
    val amountRemaining: Double = 0.0
)

data class EditableOrderItem(
    val tempEditorId: String = UUID.randomUUID().toString(),
    val product: Product? = null,
    val selectedUnit: Unit? = null,
    val quantity: String = "1",
    val sellingPrice: String = "0.0",
    val lineTotal: Double = 0.0,
    val lineGain: Double = 0.0
)

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

    data class UpdatePaymentType(val type: PaymentType) : OrderEvent
    data class UpdateAmountPaid(val amount: String) : OrderEvent
    data class UpdateItemProduct(val tempEditorId: String, val product: Product?) : OrderEvent
    data class UpdateItemUnit(val tempEditorId: String, val unit: Unit?) : OrderEvent
    data class UpdateItemQuantity(val tempEditorId: String, val quantity: String) : OrderEvent
    data class UpdateItemPrice(val tempEditorId: String, val price: String) : OrderEvent

    data object SaveOrder : OrderEvent
    data object ClearSnackbar : OrderEvent
    data object OpenNewOrderForm : OrderEvent
}
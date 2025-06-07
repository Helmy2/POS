package com.wael.astimal.pos.features.client_management.presentaion.sales_return

import androidx.annotation.StringRes
import com.wael.astimal.pos.features.client_management.domain.entity.Client
import com.wael.astimal.pos.features.client_management.domain.entity.PaymentType
import com.wael.astimal.pos.features.client_management.domain.entity.SalesReturn
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.Unit
import com.wael.astimal.pos.features.user.domain.entity.User
import java.util.UUID

data class SalesReturnScreenState(
    val loading: Boolean = false,
    val returns: List<SalesReturn> = emptyList(),
    val selectedReturn: SalesReturn? = null,

    val newReturnInput: EditableSalesReturn = EditableSalesReturn(),
    val availableClients: List<Client> = emptyList(),
    val availableProducts: List<Product> = emptyList(),
    val availableEmployees: List<User> = emptyList(),

    val query: String = "",
    @StringRes val error: Int? = null,
    @StringRes val snackbarMessage: Int? = null,

    val isQueryActive: Boolean = false,
    val isDetailViewOpen: Boolean = false,
)

data class EditableSalesReturn(
    val selectedClient: Client? = null,
    val selectedEmployeeId: Long? = null,
    val paymentType: PaymentType = PaymentType.CASH,
    val items: List<EditableReturnItem> = listOf(),
    val amountRefunded: String = "0.0",
    val totalReturnValue: Double = 0.0,
    val totalGainLoss: Double = 0.0,
    val newDebt: Double = 0.0
)

data class EditableReturnItem(
    val tempEditorId: String = UUID.randomUUID().toString(),
    val product: Product? = null,
    val selectedUnit: com.wael.astimal.pos.features.inventory.domain.entity.Unit? = null,
    val quantity: String = "1",
    val priceAtReturn: String = "0.0",
    val lineTotal: Double = 0.0,
    val lineGainLoss: Double = 0.0
)

sealed interface SalesReturnScreenEvent {
    data class SearchReturns(val query: String) : SalesReturnScreenEvent
    data class SelectReturnToView(val salesReturn: SalesReturn?) : SalesReturnScreenEvent
    data class UpdateQuery(val query: String) : SalesReturnScreenEvent
    data class UpdateIsQueryActive(val isActive: Boolean) : SalesReturnScreenEvent

    data object OpenNewReturnForm : SalesReturnScreenEvent
    data object CloseReturnForm : SalesReturnScreenEvent
    data class SelectClient(val client: Client?) : SalesReturnScreenEvent
    data class UpdatePaymentType(val type: PaymentType) : SalesReturnScreenEvent
    data class UpdateAmountRefunded(val amount: String) : SalesReturnScreenEvent
    data object AddItemToReturn : SalesReturnScreenEvent
    data class RemoveItemFromReturn(val tempEditorId: String) : SalesReturnScreenEvent
    data class UpdateItemProduct(val tempEditorId: String, val product: Product?) : SalesReturnScreenEvent
    data class UpdateItemUnit(val tempEditorId: String, val unit: Unit?) : SalesReturnScreenEvent
    data class UpdateItemQuantity(val tempEditorId: String, val quantity: String) : SalesReturnScreenEvent
    data class UpdateItemPrice(val tempEditorId: String, val price: String) : SalesReturnScreenEvent

    data object SaveReturn : SalesReturnScreenEvent
    data object ClearSnackbar : SalesReturnScreenEvent
}
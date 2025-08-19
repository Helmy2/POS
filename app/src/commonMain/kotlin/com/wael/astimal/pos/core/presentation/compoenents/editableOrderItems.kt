package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.management.data.local.entity.PaymentMethod
import com.wael.astimal.pos.features.management.domain.entity.EditableItem
import org.jetbrains.compose.resources.stringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.add_item
import pos.app.generated.resources.amount_paid
import pos.app.generated.resources.payment_type

@Composable
fun FlowRowScope.EditableOrderItems(
    totalAmount: Double,
    amountRemaining: Double,
    partnerBalance: Double,
    partnerBalanceAfterThisOrder: Double,
    amountPaid: String,
    itemList: List<EditableItem>,
    selectedPaymentType: PaymentMethod?,
    onUpdateAmountPaid: (String) -> Unit,
    onAddNewItemToOrder: () -> Unit,
    availableProducts: List<Product>,
    onSelectPaymentType: (PaymentMethod?) -> Unit,
    onItemProductChanged: (tempEditorId: String, product: Product?) -> Unit,
    onRemoveItemFromOrder: (tempEditorId: String) -> Unit,
    onUpdateItemMaxUnitQuantity: (tempEditorId: String, quantity: String) -> Unit,
    onUpdateItemMinUnitQuantity: (tempEditorId: String, quantity: String) -> Unit,
    onUpdateItemUnit: (tempEditorId: String, isMaxUnitSelected: Boolean) -> Unit,
    onUpdateItemMaxUnitPrice: (tempEditorId: String, price: String) -> Unit,
    onUpdateItemMinUnitPrice: (tempEditorId: String, price: String) -> Unit,
) {
    itemList.forEach { item ->
        OrderItemRow(
            item = item,
            availableProducts = availableProducts,
            onUpdateSelectedItem = onItemProductChanged,
            onRemoveItemFromOrder = onRemoveItemFromOrder,
            onUpdateItemUnit = onUpdateItemUnit,
            onUpdateItemMaxUnitQuantity = onUpdateItemMaxUnitQuantity,
            onUpdateItemMinUnitQuantity = onUpdateItemMinUnitQuantity,
            onUpdateItemMaxUnitPrice = onUpdateItemMaxUnitPrice,
            onUpdateItemMinUnitPrice = onUpdateItemMinUnitPrice,
        )
    }


    AppButton(
        onClick = { onAddNewItemToOrder() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.add_item))
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(stringResource(Res.string.add_item))
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExposedDropdownMenu(
            label = stringResource(Res.string.payment_type),
            options = PaymentMethod.entries.map { stringResource(it.stringResource()) },
            initialText = selectedPaymentType?.let { stringResource(it.stringResource()) } ?: "",
            onItemSelected = {
                onSelectPaymentType(it?.let { PaymentMethod.entries.getOrNull(it) })
            },
        )

        LabeledTextField(
            value = amountPaid,
            onValueChange = onUpdateAmountPaid,
            label = stringResource(Res.string.amount_paid),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            enabled = true,
        )
    }

    OrderTotalsSection(
        totalAmount = totalAmount,
        amountPaid = amountPaid.toDoubleOrNull() ?: 0.0,
        amountRemaining = amountRemaining,
        partnerBalance = partnerBalance,
        partnerBalanceAfterThisOrder = partnerBalanceAfterThisOrder,
    )
}
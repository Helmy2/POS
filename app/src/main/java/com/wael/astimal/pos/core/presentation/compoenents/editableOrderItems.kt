package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.management.domain.entity.EditableItem
import com.wael.astimal.pos.features.management.domain.entity.PaymentType

fun LazyStaggeredGridScope.editableOrderItems(
    totalAmount: String,
    amountRemaining: String,
    itemList: List<EditableItem>,
    selectedPaymentType: PaymentType,
    amountPaid: String,
    onUpdateAmountPaid: (String) -> Unit,
    onAddNewItemToOrder: () -> Unit,
    availableProducts: List<Product>,
    onSelectPaymentType: (PaymentType) -> Unit,
    onItemProductChanged: (tempEditorId: String, product: Product?) -> Unit,
    onRemoveItemFromOrder: (tempEditorId: String) -> Unit,
    onUpdateItemMaxUnitQuantity: (tempEditorId: String, quantity: String) -> Unit,
    onUpdateItemMinUnitQuantity: (tempEditorId: String, quantity: String) -> Unit,
    onUpdateItemUnit: (tempEditorId: String, isMaxUnitSelected: Boolean) -> Unit,
    onUpdateItemMaxUnitPrice: (tempEditorId: String, price: String) -> Unit,
    onUpdateItemMinUnitPrice: (tempEditorId: String, price: String) -> Unit,
) {
    items(itemList) { item ->
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
            modifier = Modifier.padding(8.dp),
        )
    }
    item(
        span = StaggeredGridItemSpan.FullLine
    ) {
        val context = LocalContext.current
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { onAddNewItemToOrder() },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(8.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_item))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.add_item))
            }

            CustomExposedDropdownMenu(
                label = stringResource(R.string.payment_type),
                items = PaymentType.entries,
                currentSelection = stringResource(selectedPaymentType.stringResource()),
                onItemSelected = onSelectPaymentType,
                itemToDisplayString = { context.getString(it.stringResource()) },
                modifier = Modifier.padding(8.dp),
            )

            LabeledTextField(
                value = amountPaid,
                onValueChange = onUpdateAmountPaid,
                label = stringResource(R.string.amount_paid),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                enabled = true,
                modifier = Modifier.padding(8.dp),
            )

            OrderTotalsSection(
                totalAmount = totalAmount.toDoubleOrNull() ?: 0.0,
                amountPaid = amountPaid.toDoubleOrNull() ?: 0.0,
                amountRemaining = amountRemaining.toDoubleOrNull() ?: 0.0,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}
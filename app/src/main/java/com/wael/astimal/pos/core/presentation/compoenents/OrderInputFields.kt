package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import com.wael.astimal.pos.features.management.domain.entity.EditableItem
import com.wael.astimal.pos.features.management.domain.entity.PaymentType

@Composable
fun OrderInputFields(
    itemList: List<EditableItem>,
    selectedPaymentType: PaymentType,
    amountPaid: String,
    onUpdateAmountPaid: (String) -> Unit,
    onAddNewItemToOrder: () -> Unit,
    availableProducts: List<Product>,
    onSelectPaymentType: (PaymentType?) -> Unit,
    onItemSelected: (tempEditorId: String, product: Product?) -> Unit,
    onRemoveItemFromOrder: (tempEditorId: String) -> Unit,
    onUpdateItemQuantity: (tempEditorId: String, quantity: String) -> Unit,
    onUpdateItemUnit: (tempEditorId: String, unit: ProductUnit?) -> Unit,
    onUpdateItemPrice: (tempEditorId: String, price: String) -> Unit,
) {
    val context = LocalContext.current
    Column {
        Text(stringResource(R.string.items), style = MaterialTheme.typography.titleMedium)

        itemList.forEach { item ->
            OrderItemRow(
                item = item,
                availableProducts = availableProducts,
                onUpdateSelectedItem = onItemSelected,
                onRemoveItemFromOrder = onRemoveItemFromOrder,
                onUpdateItemQuantity = onUpdateItemQuantity,
                onUpdateItemUnit = onUpdateItemUnit,
                onUpdateItemPrice = onUpdateItemPrice,
            )
        }

        Button(
            onClick = { onAddNewItemToOrder() },
            modifier = Modifier
                .align(Alignment.End)
                .padding(8.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_item))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.add_item))
        }

        CustomExposedDropdownMenu(
            label = stringResource(R.string.payment_type),
            items = PaymentType.entries,
            selectedItemId = selectedPaymentType.ordinal.toLong(),
            onItemSelected = onSelectPaymentType,
            itemToDisplayString = { context.getString(it.stringResource()) },
            itemToId = { it.ordinal.toLong() })

        TextInputField(
            value = amountPaid,
            onValueChange = onUpdateAmountPaid,
            label = stringResource(R.string.amount_paid),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
private fun OrderItemRow(
    item: EditableItem,
    availableProducts: List<Product>,
    onUpdateSelectedItem: (tempEditorId: String, product: Product?) -> Unit,
    onRemoveItemFromOrder: (tempEditorId: String) -> Unit,
    onUpdateItemQuantity: (tempEditorId: String, quantity: String) -> Unit,
    onUpdateItemUnit: (tempEditorId: String, unit: ProductUnit?) -> Unit,
    onUpdateItemPrice: (tempEditorId: String, price: String) -> Unit,
) {
    val language = LocalAppLocale.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.product),
                    items = availableProducts,
                    selectedItemId = item.product?.localId,
                    onItemSelected = { onUpdateSelectedItem(item.tempEditorId, it) },
                    itemToDisplayString = { it.localizedName.displayName(language) },
                    itemToId = { it.localId })
            }
            IconButton(onClick = { onRemoveItemFromOrder(item.tempEditorId) }) {
                Icon(Icons.Default.Delete, stringResource(R.string.remove_item))
            }
        }
        CustomExposedDropdownMenu(
            label = stringResource(R.string.unit),
            items = listOf(
                item.product?.minimumProductUnit, item.product?.maximumProductUnit
            ),
            selectedItemId = item.selectedProductUnit?.localId
                ?: item.product?.minimumProductUnit?.localId,
            onItemSelected = { unit -> onUpdateItemUnit(item.tempEditorId, unit) },
            itemToDisplayString = { it?.localizedName?.displayName(language) ?: "" },
            itemToId = { it?.localId ?: -1L },
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextInputField(
                value = item.quantity,
                onValueChange = { onUpdateItemQuantity(item.tempEditorId, it) },
                label = stringResource(R.string.qty),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            TextInputField(
                value = item.price,
                onValueChange = { onUpdateItemPrice(item.tempEditorId, it) },
                label = stringResource(R.string.price),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            TextInputField(
                value = "%.2f".format(item.lineTotal),
                onValueChange = { },
                readOnly = true,
                label = stringResource(R.string.total),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
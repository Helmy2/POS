package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.management.domain.entity.EditableItem
import org.jetbrains.compose.resources.stringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.in_stock_with_args
import pos.app.generated.resources.price
import pos.app.generated.resources.product
import pos.app.generated.resources.qty
import pos.app.generated.resources.remove_item
import pos.app.generated.resources.total
import pos.app.generated.resources.unit

@Composable
fun OrderItemRow(
    item: EditableItem,
    availableProducts: List<Product>,
    onUpdateSelectedItem: (tempEditorId: String, product: Product?) -> Unit,
    onRemoveItemFromOrder: (tempEditorId: String) -> Unit,
    onUpdateItemMaxUnitQuantity: (tempEditorId: String, quantity: String) -> Unit,
    onUpdateItemMinUnitQuantity: (tempEditorId: String, quantity: String) -> Unit,
    onUpdateItemUnit: (tempEditorId: String, isMaxUnitSelected: Boolean) -> Unit,
    onUpdateItemMaxUnitPrice: (tempEditorId: String, price: String) -> Unit,
    onUpdateItemMinUnitPrice: (tempEditorId: String, price: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLocale.current
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                CustomExposedDropdownMenu(
                    currentSelection = item.product?.name?.displayName(language) ?: "",
                    label = stringResource(Res.string.product),
                    items = availableProducts,
                    onItemSelected = { onUpdateSelectedItem(item.tempEditorId, it) },
                    itemToDisplayString = { it.name.displayName(language) },
                )
            }
            IconButton(
                onClick = { onRemoveItemFromOrder(item.tempEditorId) },
                modifier = Modifier.padding(vertical = OutlinedTextFieldDefaults.MinHeight / 6)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.remove_item),
                    modifier = Modifier.size(
                        OutlinedTextFieldDefaults.MinHeight / 1.5f
                    )
                )
            }
        }

        AnimatedVisibility(item.product?.mainProductUnit != null) {
            Text(
                text = stringResource(
                    Res.string.in_stock_with_args, item.currentStock
                ) + " " + item.product?.mainProductUnit?.name?.displayName(language).orEmpty(),
                style = MaterialTheme.typography.bodySmall
            )
        }


        AnimatedVisibility(item.product?.subProductUnit != null) {
            Column {
                CustomExposedDropdownMenu(
                    label = stringResource(Res.string.unit),
                    items = listOfNotNull(
                        item.product?.subProductUnit, item.product?.mainProductUnit
                    ),
                    currentSelection = if (item.isSelectedUnitIsMax) {
                        item.product?.mainProductUnit?.name?.displayName(language).orEmpty()
                    } else {
                        item.product?.subProductUnit?.name?.displayName(language).orEmpty()
                    },
                    onItemSelected = { unit ->
                        onUpdateItemUnit(
                            item.tempEditorId,
                            unit.id.local == item.product?.mainProductUnit?.id?.local
                        )
                    },
                    itemToDisplayString = { it.name.displayName(language) },
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Label(
                        item.product?.subProductUnit?.name?.displayName(language) ?: "",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    TextInputField(
                        value = item.subUnitQuantity,
                        onValueChange = { onUpdateItemMinUnitQuantity(item.tempEditorId, it) },
                        label = stringResource(Res.string.qty),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        enabled = item.isSelectedUnitIsMax.not(),
                        modifier = Modifier.height(OutlinedTextFieldDefaults.MinHeight + 6.dp)
                            .weight(1f),
                    )
                    TextInputField(
                        value = item.subUnitPrice,
                        onValueChange = { onUpdateItemMinUnitPrice(item.tempEditorId, it) },
                        label = stringResource(Res.string.price),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        enabled = item.isSelectedUnitIsMax.not(),
                        modifier = Modifier.height(OutlinedTextFieldDefaults.MinHeight + 6.dp)
                            .weight(1f),
                    )
                }
            }
        }
        AnimatedVisibility(item.product?.mainProductUnit != null) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Label(
                        item.product?.mainProductUnit?.name?.displayName(language) ?: ""
                    )
                    TextInputField(
                        value = item.mainUnitQuantity,
                        onValueChange = { onUpdateItemMaxUnitQuantity(item.tempEditorId, it) },
                        label = stringResource(Res.string.qty),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        enabled = item.isSelectedUnitIsMax,
                        modifier = Modifier.height(OutlinedTextFieldDefaults.MinHeight + 6.dp)
                            .weight(1f),
                    )
                    TextInputField(
                        value = item.mainUnitPrice,
                        onValueChange = { onUpdateItemMaxUnitPrice(item.tempEditorId, it) },
                        label = stringResource(Res.string.price),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        enabled = item.isSelectedUnitIsMax,
                        modifier = Modifier.height(OutlinedTextFieldDefaults.MinHeight + 6.dp)
                            .weight(1f),
                    )
                }

                Card(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(Res.string.total),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "%.2f".format(item.lineTotal),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
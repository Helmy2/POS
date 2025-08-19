package com.wael.astimal.pos.core.presentation.compoenents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.management.domain.entity.EditableItem
import org.jetbrains.compose.resources.stringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.in_stock_with_args
import pos.app.generated.resources.no_selection
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
        modifier.width(320.dp).clip(RoundedCornerShape(16.dp)).background(
            MaterialTheme.colorScheme.primary.copy(alpha = .1f)
        ).padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ExposedDropdownMenu(
                options = availableProducts.map { it.name.displayName(language) },
                onItemSelected = {
                    onUpdateSelectedItem(
                        item.tempEditorId, it?.let { availableProducts[it] })
                },
                label = stringResource(Res.string.product),
                noSelectionText = stringResource(Res.string.no_selection),
                initialText = item.product?.name?.displayName(language) ?: "",
                modifier = Modifier.width(250.dp)
            )
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
                ExposedDropdownMenu(
                    label = stringResource(Res.string.unit),
                    options = listOfNotNull(
                        item.product?.subProductUnit, item.product?.mainProductUnit
                    ).map { it.name.displayName(language) },
                    initialText = if (item.isSelectedUnitIsMax) {
                        item.product?.mainProductUnit?.name?.displayName(language).orEmpty()
                    } else {
                        item.product?.subProductUnit?.name?.displayName(language).orEmpty()
                    },
                    onItemSelected = {
                        onUpdateItemUnit(
                            item.tempEditorId, it == 0
                        )
                    },
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LabeledTextField(
                        item.product?.subProductUnit?.name?.displayName(language) ?: "",
                        onValueChange = {},
                        label = stringResource(Res.string.unit),
                        enabled = false,
                        modifier = Modifier.weight(1f),
                    )
                    LabeledTextField(
                        value = item.subUnitQuantity,
                        onValueChange = { onUpdateItemMinUnitQuantity(item.tempEditorId, it) },
                        label = stringResource(Res.string.qty),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        enabled = item.isSelectedUnitIsMax.not(),
                        modifier = Modifier.weight(1f),
                    )
                    LabeledTextField(
                        value = item.subUnitPrice,
                        onValueChange = { onUpdateItemMinUnitPrice(item.tempEditorId, it) },
                        label = stringResource(Res.string.price),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        enabled = item.isSelectedUnitIsMax.not(),
                        modifier = Modifier.weight(1f),
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
                    LabeledTextField(
                        value = item.product?.mainProductUnit?.name?.displayName(language) ?: "",
                        onValueChange = {},
                        label = stringResource(Res.string.unit),
                        enabled = false,
                        modifier = Modifier.weight(1f),
                    )
                    LabeledTextField(
                        value = item.mainUnitQuantity,
                        onValueChange = { onUpdateItemMaxUnitQuantity(item.tempEditorId, it) },
                        label = stringResource(Res.string.qty),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        enabled = item.isSelectedUnitIsMax,
                        modifier = Modifier.weight(1f),
                    )
                    LabeledTextField(
                        value = item.mainUnitPrice,
                        onValueChange = { onUpdateItemMaxUnitPrice(item.tempEditorId, it) },
                        label = stringResource(Res.string.price),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        enabled = item.isSelectedUnitIsMax,
                        modifier = Modifier.weight(1f),
                    )
                }

                Card(
                    modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth()
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
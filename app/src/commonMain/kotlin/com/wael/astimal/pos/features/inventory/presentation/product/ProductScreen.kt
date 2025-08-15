package com.wael.astimal.pos.features.inventory.presentation.product

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.presentation.compoenents.ConfirmDeleteDialog
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen2
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.ar_name
import pos.app.generated.resources.average_cost_price
import pos.app.generated.resources.category
import pos.app.generated.resources.current_stock
import pos.app.generated.resources.en_name
import pos.app.generated.resources.main_unit
import pos.app.generated.resources.purchase_price
import pos.app.generated.resources.selling_price
import pos.app.generated.resources.sub_unit
import pos.app.generated.resources.sub_unit_per_main_unit

@Composable
fun ProductRoute(
    modifier: Modifier = Modifier,
    viewModel: ProductViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProductScreen(
        state = state, onEvent = viewModel::processEvent, modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    state: ProductContract.State,
    onEvent: (ProductContract.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppLocale.current

    SearchScreen2(
        modifier = modifier,
        query = state.searchQuery,
        isSearchActive = state.isSearchActive,
        isNew = !state.isEditing,
        onQueryChange = { onEvent(ProductContract.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(ProductContract.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(ProductContract.Event.SearchActiveChanged(it)) },
        onBack = { onEvent(ProductContract.Event.BackClicked) },
        lastModifiedDate = state.selectedProduct?.product?.updatedAt,
        onDelete = { onEvent(ProductContract.Event.DeleteClicked) },
        onCreate = { onEvent(ProductContract.Event.SaveClicked) },
        onUpdate = { onEvent(ProductContract.Event.SaveClicked) },
        onNew = { onEvent(ProductContract.Event.NewProductClicked) },
        canEdit = state.canUserEdit,
        canSave = state.canSave,
        searchResults = {
            ItemGrid(
                list = state.products,
                onItemClick = { product -> onEvent(ProductContract.Event.ProductSelected(product)) },
                label = {
                    Label(
                        it.product.name.displayName(language) + " : " + if (state.canUserEdit) {
                            stringResource(Res.string.average_cost_price) + " : " +
                                    String.format("%.2f", it.product.averagePrice)
                        } else {
                            ""
                        } + " : " + stringResource(Res.string.current_stock) + " : " +
                                String.format("%.2f", it.stock)
                    )
                },
                isSelected = { product -> product.product.id == state.selectedProduct?.product?.id },
            )
        },
        mainContent = {
            if (state.selectedProduct?.product?.averagePrice.takeIf { it != 0.0 } != null && state.canUserEdit) {
                item {
                    Row {
                        Label(
                            text = stringResource(Res.string.average_cost_price),
                            modifier = Modifier.padding(8.dp)
                        )
                        Label(
                            text = String.format(
                                "%.2f",
                                state.selectedProduct?.product?.averagePrice
                            ),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
            if (state.selectedProduct?.stock != null) {
                item {
                    Row {
                        Label(
                            text = stringResource(Res.string.current_stock),
                            modifier = Modifier.padding(8.dp)
                        )
                        Label(
                            text = String.format("%.2f", state.selectedProduct.stock),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
            item {
                LabeledTextField(
                    value = state.inputEnName,
                    onValueChange = { onEvent(ProductContract.Event.EnNameChanged(it)) },
                    label = stringResource(Res.string.en_name),
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)
                )
            }
            item {
                LabeledTextField(
                    value = state.inputArName,
                    onValueChange = { onEvent(ProductContract.Event.ArNameChanged(it)) },
                    label = stringResource(Res.string.ar_name),
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)
                )
            }
            item {
                LabeledTextField(
                    value = state.inputPurchasePrice,
                    onValueChange = { onEvent(ProductContract.Event.PurchasePriceChanged(it)) },
                    label = stringResource(Res.string.purchase_price),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)
                )
            }
            item {
                LabeledTextField(
                    value = state.inputSellingPrice,
                    onValueChange = { onEvent(ProductContract.Event.SellingPriceChanged(it)) },
                    label = stringResource(Res.string.selling_price),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)
                )
            }
            item {
                CustomExposedDropdownMenu(
                    label = stringResource(Res.string.category),
                    items = state.dropdownData.categories,
                    selectedItemId = state.selectedCategoryId,
                    onItemSelected = { onEvent(ProductContract.Event.CategoryIdChanged(it.id)) },
                    itemToDisplayString = { it.name.displayName(language) },
                    itemToId = { it.id },
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)
                )
            }
            item {
                CustomExposedDropdownMenu(
                    label = stringResource(Res.string.main_unit),
                    items = state.dropdownData.units,
                    selectedItemId = state.selectedMainUnitId,
                    onItemSelected = { onEvent(ProductContract.Event.MainUnitIdChanged(it.id)) },
                    itemToDisplayString = { it.name.displayName(language) },
                    itemToId = { it.id },
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)
                )
            }

            item {
                CustomExposedDropdownMenu(
                    label = stringResource(Res.string.sub_unit),
                    items = state.dropdownData.units,
                    selectedItemId = state.selectedSubUnitId,
                    onItemSelected = { onEvent(ProductContract.Event.SubUnitIdChanged(it.id)) },
                    itemToDisplayString = { it.name.displayName(language) },
                    itemToId = { it.id },
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)
                )
            }

            item {
                LabeledTextField(
                    value = state.inputSubUnitsPerMainUnit,
                    onValueChange = { onEvent(ProductContract.Event.SubUnitsPerMainUnitChanged(it)) },
                    label = stringResource(Res.string.sub_unit_per_main_unit),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)
                )
            }

            item {
                ConfirmDeleteDialog(
                    onConfirm = { onEvent(ProductContract.Event.DeleteConfirmed) },
                    onDismiss = { onEvent(ProductContract.Event.DeleteCanceled) },
                    show = state.showDeleteDialog
                )
            }
        },
    )
}

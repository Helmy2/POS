package com.wael.astimal.pos.features.inventory.presentation.product

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.ar_name
import pos.app.generated.resources.category
import pos.app.generated.resources.en_name
import pos.app.generated.resources.max_stock_unit
import pos.app.generated.resources.min_stock_unit
import pos.app.generated.resources.min_unit_per_max_unit
import pos.app.generated.resources.opening_balance_qty
import pos.app.generated.resources.purchase_price
import pos.app.generated.resources.selling_price
import pos.app.generated.resources.store

@Composable
fun ProductRoute(
    modifier: Modifier = Modifier,
    viewModel: ProductViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProductScreen(
        state = state,
        onEvent = viewModel::processEvent,
        modifier = modifier
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

    SearchScreen(
        modifier = modifier,
        query = state.searchQuery,
        isSearchActive = state.isSearchActive,
        isNew = !state.isEditing,
        onQueryChange = { onEvent(ProductContract.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(ProductContract.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(ProductContract.Event.SearchActiveChanged(it)) },
        onBack = { onEvent(ProductContract.Event.BackClicked) },
        lastModifiedDate = state.selectedProduct?.updatedAt,
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
                label = { Label(it.name.displayName(language)) },
                isSelected = { product -> product.id.local == state.selectedProduct?.id?.local },
            )
        },
        mainContent = {
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
                LabeledTextField(
                    value = state.inputOpeningBalance,
                    onValueChange = { onEvent(ProductContract.Event.OpeningBalanceChanged(it)) },
                    label = stringResource(Res.string.opening_balance_qty),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = state.canUserEdit && !state.isEditing,
                    modifier = Modifier.padding(8.dp)
                )
            }
            item {
                CustomExposedDropdownMenu(
                    label = stringResource(Res.string.category),
                    items = state.dropdownData.categories,
                    selectedItemId = state.selectedCategoryId,
                    onItemSelected = { onEvent(ProductContract.Event.CategoryIdChanged(it.id.local)) },
                    itemToDisplayString = { it.name.displayName(language) },
                    itemToId = { it.id.local },
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)
                )
            }
            item {
                CustomExposedDropdownMenu(
                    label = stringResource(Res.string.store),
                    items = state.dropdownData.stores,
                    selectedItemId = state.selectedStoreId,
                    onItemSelected = { onEvent(ProductContract.Event.StoreIdChanged(it.id.local)) },
                    itemToDisplayString = { it.name.displayName(language) },
                    itemToId = { it.id.local },
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)
                )
            }
            item {
                CustomExposedDropdownMenu(
                    label = stringResource(Res.string.max_stock_unit),
                    items = state.dropdownData.units,
                    selectedItemId = state.selectedMaximumUnitId,
                    onItemSelected = { onEvent(ProductContract.Event.MaximumUnitIdChanged(it.id.local)) },
                    itemToDisplayString = { it.name.displayName(language) },
                    itemToId = { it.id.local },
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)
                )
            }
            item {
                LabeledTextField(
                    value = state.inputSubUnitsPerMainUnit,
                    onValueChange = { onEvent(ProductContract.Event.SubUnitsPerMainUnitChanged(it)) },
                    label = stringResource(Res.string.min_unit_per_max_unit),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)
                )
            }
            item {
                CustomExposedDropdownMenu(
                    label = stringResource(Res.string.min_stock_unit),
                    items = state.dropdownData.units,
                    selectedItemId = state.selectedMinimumUnitId,
                    onItemSelected = { onEvent(ProductContract.Event.MinimumUnitIdChanged(it.id.local)) },
                    itemToDisplayString = { it.name.displayName(language) },
                    itemToId = { it.id.local },
                    canClearSelection = true,
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
    )
}

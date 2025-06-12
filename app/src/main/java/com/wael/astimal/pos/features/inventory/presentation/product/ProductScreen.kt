package com.wael.astimal.pos.features.inventory.presentation.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProductRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProductScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    state: ProductState,
    onEvent: (ProductEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppLocale.current
    SearchScreen(
        modifier = modifier,
        query = state.query,
        isSearchActive = state.isQueryActive,
        loading = state.loading,
        isNew = state.isNew,
        onQueryChange = { onEvent(ProductEvent.UpdateQuery(it)) },
        onSearch = { onEvent(ProductEvent.Search(it)) },
        onSearchActiveChange = { onEvent(ProductEvent.UpdateIsQueryActive(it)) },
        onBack = onBack,
        lastModifiedDate = state.selectedProduct?.lastModified,
        onDelete = { onEvent(ProductEvent.DeleteProduct) },
        onCreate = { onEvent(ProductEvent.SaveProduct) },
        onUpdate = { onEvent(ProductEvent.SaveProduct) },
        onNew = { onEvent(ProductEvent.SelectProduct(null)) },
        searchResults = {
            ItemGrid(
                list = state.searchResults,
                onItemClick = { product ->
                    onEvent(ProductEvent.UpdateIsQueryActive(false))
                    onEvent(ProductEvent.SelectProduct(product))
                },
                label = { Text(it.localizedName.displayName(language)) },
                isSelected = { product -> product.localId == state.selectedProduct?.localId },
            )
        },
        mainContent = {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state.error != null) {
                    Text(state.error, color = MaterialTheme.colorScheme.error)
                }

                LabeledTextField(
                    value = state.inputArName,
                    onValueChange = { onEvent(ProductEvent.UpdateInputArName(it)) },
                    label = stringResource(id = R.string.ar_name),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                LabeledTextField(
                    value = state.inputEnName,
                    onValueChange = { onEvent(ProductEvent.UpdateInputEnName(it)) },
                    label = stringResource(id = R.string.en_name),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // Category Dropdown
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.categories),
                    items = state.categories,
                    selectedItemId = state.selectedCategoryId,
                    onItemSelected = { category -> onEvent(ProductEvent.SelectCategoryId(category?.localId)) },
                    itemToDisplayString = { it.localizedName.displayName(language) },
                    itemToId = { it.localId })

                LabeledTextField(
                    value = state.inputAveragePrice,
                    onValueChange = { onEvent(ProductEvent.UpdateInputAveragePrice(it)) },
                    label = stringResource(R.string.average_cost_price),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next
                    )
                )
                LabeledTextField(
                    value = state.inputSellingPrice,
                    onValueChange = { onEvent(ProductEvent.UpdateInputSellingPrice(it)) },
                    label = stringResource(R.string.selling_price),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next
                    )
                )
                LabeledTextField(
                    value = state.inputOpeningBalance,
                    onValueChange = { onEvent(ProductEvent.UpdateInputOpeningBalance(it)) },
                    label = stringResource(R.string.opening_balance_qty),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next
                    )
                )

                // Store Dropdown
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.stores),
                    items = state.stores,
                    selectedItemId = state.selectedStoreId,
                    onItemSelected = { store -> onEvent(ProductEvent.SelectStoreId(store?.localId)) },
                    itemToDisplayString = { it.localizedName.displayName(language) },
                    itemToId = { it.localId })

                LabeledTextField(
                    value = state.inputMinStockLevel,
                    onValueChange = { onEvent(ProductEvent.UpdateInputMinStockLevel(it)) },
                    label = stringResource(R.string.min_stock_level),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                    )
                )
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.min_stock_unit),
                    items = state.units,
                    selectedItemId = state.selectedMinStockUnitId,
                    onItemSelected = { unit -> onEvent(ProductEvent.SelectMinStockUnitId(unit?.localId)) },
                    itemToDisplayString = { "${it.enName}: ${it.arName}" },
                    itemToId = { it.localId })

                LabeledTextField(
                    value = state.inputMaxStockLevel,
                    onValueChange = { onEvent(ProductEvent.UpdateInputMaxStockLevel(it)) },
                    label = stringResource(R.string.max_stock_level),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                    )
                )
                CustomExposedDropdownMenu(
                    label = stringResource(R.string.max_stock_unit),
                    items = state.units,
                    selectedItemId = state.selectedMaxStockUnitId,
                    onItemSelected = { unit -> onEvent(ProductEvent.SelectMaxStockUnitId(unit?.localId)) },
                    itemToDisplayString = { "${it.enName}: ${it.arName}" },
                    itemToId = { it.localId })

                LabeledTextField(
                    value = state.inputFirstPeriodData,
                    onValueChange = { onEvent(ProductEvent.UpdateInputFirstPeriodData(it)) },
                    label = stringResource(R.string.first_period_data),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
            }
        },
    )
}


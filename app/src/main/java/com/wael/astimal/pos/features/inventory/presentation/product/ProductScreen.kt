package com.wael.astimal.pos.features.inventory.presentation.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.base.UiEvent
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import kotlinx.coroutines.flow.SharedFlow
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
        eventFlow = viewModel.eventFlow,
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    state: ProductState,
    onEvent: (ProductEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    eventFlow: SharedFlow<UiEvent>,
) {
    val language = LocalAppLocale.current
    SearchScreen(
        eventFlow = eventFlow,
        modifier = modifier,
        query = state.query,
        isSearchActive = state.isQueryActive,
        loading = state.loading,
        isNew = state.isNew,
        onQueryChange = { onEvent(ProductEvent.UpdateQuery(it)) },
        onSearch = { onEvent(ProductEvent.Search(it)) },
        onSearchActiveChange = { onEvent(ProductEvent.UpdateIsQueryActive(it)) },
        onBack = onBack,
        lastModifiedDate = state.selectedProduct?.updatedAt,
        onDelete = { onEvent(ProductEvent.DeleteProduct) },
        onCreate = { onEvent(ProductEvent.SaveProduct) },
        onUpdate = { onEvent(ProductEvent.SaveProduct) },
        onNew = { onEvent(ProductEvent.SelectProduct(null)) },
        canEdit = state.canEdit,
        searchResults = {
            ItemGrid(
                list = state.searchResults,
                onItemClick = { product ->
                    onEvent(ProductEvent.UpdateIsQueryActive(false))
                    onEvent(ProductEvent.SelectProduct(product))
                },
                label = { Label(it.localizedName.displayName(language)) },
                isSelected = { product -> product.id.local == state.selectedProduct?.id?.local },
            )
        },
        mainContent = {
            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LabeledTextField(
                    value = state.inputArName,
                    onValueChange = { onEvent(ProductEvent.UpdateInputArName(it)) },
                    label = stringResource(id = R.string.ar_name),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    enabled = state.canEdit
                )
                LabeledTextField(
                    value = state.inputEnName,
                    onValueChange = { onEvent(ProductEvent.UpdateInputEnName(it)) },
                    label = stringResource(id = R.string.en_name),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    enabled = state.canEdit
                )


                CustomExposedDropdownMenu(
                    label = stringResource(R.string.categories),
                    items = state.categories,
                    selectedItemId = state.selectedCategoryId,
                    onItemSelected = { category -> onEvent(ProductEvent.SelectCategoryId(category?.id?.local)) },
                    itemToDisplayString = { it.localizedName.displayName(language) },
                    itemToId = { it.id.local },
                    canClearSelection = false,
                    enabled = state.canEdit
                )

                LabeledTextField(
                    value = state.inputAveragePrice,
                    onValueChange = { onEvent(ProductEvent.UpdateInputAveragePrice(it)) },
                    label = stringResource(R.string.average_cost_price),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next
                    ),
                    enabled = state.canEdit
                )
                LabeledTextField(
                    value = state.inputSellingPrice,
                    onValueChange = { onEvent(ProductEvent.UpdateInputSellingPrice(it)) },
                    label = stringResource(R.string.selling_price),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next
                    ),
                    enabled = state.canEdit
                )
                LabeledTextField(
                    value = state.inputOpeningBalance,
                    onValueChange = { onEvent(ProductEvent.UpdateInputOpeningBalance(it)) },
                    label = stringResource(R.string.opening_balance_qty),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next
                    ),
                    enabled = state.canEdit
                )

                CustomExposedDropdownMenu(
                    label = stringResource(R.string.stores),
                    items = state.stores,
                    selectedItemId = state.selectedStoreId,
                    onItemSelected = { store -> onEvent(ProductEvent.SelectStoreId(store?.id?.local)) },
                    itemToDisplayString = { it.name.displayName(language) },
                    itemToId = { it.id.local },
                    canClearSelection = false,
                    enabled = state.canEdit
                )

                CustomExposedDropdownMenu(
                    label = stringResource(R.string.min_stock_unit),
                    items = state.units,
                    selectedItemId = state.selectedMinStockUnitId,
                    onItemSelected = { unit -> onEvent(ProductEvent.SelectMinStockUnitId(unit?.localId)) },
                    itemToDisplayString = { "${it.enName}: ${it.arName}" },
                    itemToId = { it.localId },
                    canClearSelection = true,
                    enabled = state.canEdit
                )

                LabeledTextField(
                    value = state.subUnitsPerMainUnit,
                    onValueChange = { value ->
                        value.toDoubleOrNull()?.let {
                            onEvent(ProductEvent.UpdateSubUnitsPerMainUnit(value))
                        }
                    },
                    label = stringResource(R.string.min_unit_per_max_unit),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                    ),
                    enabled = state.canEdit
                )

                CustomExposedDropdownMenu(
                    label = stringResource(R.string.max_stock_unit),
                    items = state.units,
                    selectedItemId = state.selectedMaxStockUnitId,
                    onItemSelected = { unit -> onEvent(ProductEvent.SelectMaxStockUnitId(unit?.localId)) },
                    itemToDisplayString = { "${it.enName}: ${it.arName}" },
                    itemToId = { it.localId },
                    canClearSelection = false,
                    enabled = state.canEdit
                )
            }
        },
    )
}


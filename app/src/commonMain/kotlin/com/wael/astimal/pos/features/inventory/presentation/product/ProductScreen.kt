package com.wael.astimal.pos.features.inventory.presentation.product

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.domain.entity.displayName
import com.wael.astimal.pos.core.presentation.compoenents.ConfirmDeleteDialog
import com.wael.astimal.pos.core.presentation.compoenents.ExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.core.util.formate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.ar_name
import pos.app.generated.resources.average_cost_price
import pos.app.generated.resources.category
import pos.app.generated.resources.current_stock
import pos.app.generated.resources.en_name
import pos.app.generated.resources.initial_quantity
import pos.app.generated.resources.initial_store
import pos.app.generated.resources.main_unit
import pos.app.generated.resources.purchase_price
import pos.app.generated.resources.selling_price
import pos.app.generated.resources.sub_unit
import pos.app.generated.resources.sub_unit_per_main_unit

@Composable
fun ProductRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProductScreen(
        onBack = onBack,
        state = state, onEvent = viewModel::processEvent, modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    state: ProductReducer.State,
    onEvent: (ProductReducer.Event) -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val language = LocalAppLocale.current

    SearchScreen(
        modifier = modifier,
        query = state.searchQuery,
        isSearchActive = state.isSearchActive,
        isNew = !state.isEditing,
        onQueryChange = { onEvent(ProductReducer.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(ProductReducer.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(ProductReducer.Event.SearchActiveChanged(it)) },
        onBack = onBack,
        lastModifiedDate = state.selectedProduct?.product?.updatedAt,
        onDelete = { onEvent(ProductReducer.Event.DeleteClicked) },
        onCreate = { onEvent(ProductReducer.Event.SaveClicked) },
        onUpdate = { onEvent(ProductReducer.Event.SaveClicked) },
        onNew = { onEvent(ProductReducer.Event.NewProductClicked) },
        enableFab = state.enabledFab,
        canDelete = state.canDelete,
        canCreate = state.canCreate,
        canUpdate = state.canUpdate,
        searchResults = {
            ItemGrid(
                list = state.products,
                onItemClick = { product -> onEvent(ProductReducer.Event.ProductSelected(product)) },
                label = {
                    Label(
                        it.product.name.displayName(language) + " : " + if (state.canEdit) {
                            stringResource(Res.string.average_cost_price) + " : " +
                                    it.product.averagePrice.formate()
                        } else {
                            ""
                        } + " : " + stringResource(Res.string.current_stock) + " : " +
                                it.stock.formate()
                    )
                },
                isSelected = { product -> product.product.id == state.selectedProduct?.product?.id },
            )
        },
        mainContent = {
            if (state.selectedProduct?.product?.averagePrice.takeIf { it != 0.0 } != null && state.canEdit) {
                LabeledTextField(
                    value = state.selectedProduct?.product?.averagePrice.formate(),
                    onValueChange = { },
                    label = stringResource(Res.string.average_cost_price),
                    enabled = false,
                )
            }
            if (state.selectedProduct?.stock != null) {
                LabeledTextField(
                    value = state.selectedProduct.stock.formate(),
                    onValueChange = { },
                    label = stringResource(Res.string.current_stock),
                    enabled = false,
                )
            }
            LabeledTextField(
                value = state.inputEnName,
                onValueChange = { onEvent(ProductReducer.Event.EnNameChanged(it)) },
                label = stringResource(Res.string.en_name),
                enabled = state.canEdit,
            )

            LabeledTextField(
                value = state.inputArName,
                onValueChange = { onEvent(ProductReducer.Event.ArNameChanged(it)) },
                label = stringResource(Res.string.ar_name),
                enabled = state.canEdit,
            )
            LabeledTextField(
                value = state.inputPurchasePrice,
                onValueChange = { onEvent(ProductReducer.Event.PurchasePriceChanged(it)) },
                label = stringResource(Res.string.purchase_price),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                enabled = state.canEdit,
            )
            LabeledTextField(
                value = state.inputSellingPrice,
                onValueChange = { onEvent(ProductReducer.Event.SellingPriceChanged(it)) },
                label = stringResource(Res.string.selling_price),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                enabled = state.canEdit,
            )
            ExposedDropdownMenu(
                label = stringResource(Res.string.category),
                options = state.dropdownData.categories.map { it.name.displayName(language) },
                onItemSelected = {
                    onEvent(ProductReducer.Event.CategoryIdChanged(it?.let { it ->
                        state.dropdownData.categories.getOrNull(
                            it
                        )
                    }))
                },
                enabled = state.canEdit,
                initialText = state.selectedCategory?.name.displayName(language)
            )
            ExposedDropdownMenu(
                label = stringResource(Res.string.main_unit),
                options = state.dropdownData.units.map { it.name.displayName(language) },
                onItemSelected = {
                    onEvent(ProductReducer.Event.MainUnitIdChanged(it?.let { it ->
                        state.dropdownData.units.getOrNull(
                            it
                        )
                    }))
                },
                enabled = state.canEdit,
                initialText = state.selectedMainUnit?.name.displayName(language)
            )
            ExposedDropdownMenu(
                label = stringResource(Res.string.sub_unit),
                options = state.dropdownData.units.map { it.name.displayName(language) },
                onItemSelected = {
                    onEvent(ProductReducer.Event.SubUnitIdChanged(it?.let { it ->
                        state.dropdownData.units.getOrNull(
                            it
                        )
                    }))
                },
                enabled = state.canEdit,
                initialText = state.selectedSubUnit?.name.displayName(language)
            )
            LabeledTextField(
                value = state.inputSubUnitsPerMainUnit,
                onValueChange = { onEvent(ProductReducer.Event.SubUnitsPerMainUnitChanged(it)) },
                label = stringResource(Res.string.sub_unit_per_main_unit),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                enabled = state.canEdit,
            )
            if (!state.isEditing) {
                ExposedDropdownMenu(
                    label = stringResource(Res.string.initial_store),
                    options = state.dropdownData.stores.map { it.name.displayName(language) },
                    initialText = state.initialStore?.name.displayName(language),
                    onItemSelected = {
                        onEvent(
                            ProductReducer.Event.InitialStoreChanged(
                                it?.let { state.dropdownData.stores.getOrNull(it) }
                            )
                        )
                    },
                    enabled = state.canEdit,
                )
                LabeledTextField(
                    value = state.initialQuantity,
                    onValueChange = {
                        onEvent(
                            ProductReducer.Event.InitialQuantityChanged(it)
                        )
                    },
                    label = stringResource(Res.string.initial_quantity),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    enabled = state.canEdit,
                )
            }

            ConfirmDeleteDialog(
                onConfirm = { onEvent(ProductReducer.Event.DeleteConfirmed) },
                onDismiss = { onEvent(ProductReducer.Event.DeleteCanceled) },
                show = state.showDeleteDialog
            )
        },
    )
}

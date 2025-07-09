package com.wael.astimal.pos.features.inventory.presentation.stock_management

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.presentation.compoenents.ConfirmDeleteDialog
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.notes
import pos.app.generated.resources.product
import pos.app.generated.resources.quantity_change_by
import pos.app.generated.resources.reason
import pos.app.generated.resources.store

@Composable
fun StockManagementRoute(viewModel: StockManagementViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    StockManagementScreen(
        state = state, onEvent = viewModel::processEvent
    )
}

@Composable
fun StockManagementScreen(
    state: StockManagementContract.State,
    onEvent: (StockManagementContract.Event) -> Unit,
) {

    val language = LocalAppLocale.current
    SearchScreen(
        query = state.query,
        isSearchActive = state.isSearchActive,
        onQueryChange = { onEvent(StockManagementContract.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(StockManagementContract.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(StockManagementContract.Event.SearchActiveChanged(it)) },
        onBack = { onEvent(StockManagementContract.Event.NavigateBack) },
        lastModifiedDate = state.selectedAdjustment?.updatedAt,
        onDelete = { onEvent(StockManagementContract.Event.DeleteClicked) },
        onCreate = { onEvent(StockManagementContract.Event.SaveClicked) },
        onUpdate = { onEvent(StockManagementContract.Event.SaveClicked) },
        onNew = { onEvent(StockManagementContract.Event.NewStockAdjustmentClicked) },
        canEdit = state.canUserEdit,
        canSave = state.canSave,
        searchResults = {
            ItemGrid(
                list = state.stockAdjustments,
                onItemClick = { stock ->
                    onEvent(
                        StockManagementContract.Event.SelectedAdjustmentChanged(
                            stock
                        )
                    )
                },
                label = {
                    Label(
                        it.store.name.displayName(LocalAppLocale.current) + " - " + it.product.name.displayName(
                            LocalAppLocale.current
                        ) + " - " + stringResource(it.reason.getStringResource()) + " - " + it.quantityChange.toString()
                    )
                },
                isSelected = { it.id == state.selectedAdjustment?.id },
            )
        },
        isNew = state.selectedAdjustment == null,
    ) {
        item {
            CustomExposedDropdownMenu(
                label = stringResource(Res.string.store),
                items = state.stores,
                currentSelection = state.adjustmentStore?.name?.displayName(
                    LocalAppLocale.current
                ) ?: "",
                onItemSelected = {
                    onEvent(
                        StockManagementContract.Event.AdjustmentStoreChanged(it)
                    )
                },
                itemToDisplayString = { it.name.displayName(language) },
                enabled = state.canUserEdit,
                modifier = Modifier.padding(8.dp)
            )
        }
        item {
            CustomExposedDropdownMenu(
                label = stringResource(Res.string.product),
                items = state.products,
                currentSelection = state.adjustmentProduct?.name?.displayName(
                    LocalAppLocale.current
                ) ?: "",
                onItemSelected = {
                    onEvent(
                        StockManagementContract.Event.AdjustmentProductChanged(it)
                    )
                },
                itemToDisplayString = { it.name.displayName(language) },
                enabled = state.canUserEdit,
                modifier = Modifier.padding(8.dp)
            )
        }

        item {
            LabeledTextField(
                value = state.adjustmentQuantityChange,
                onValueChange = {
                    onEvent(
                        StockManagementContract.Event.AdjustmentQuantityChanged(
                            it
                        )
                    )
                },
                label = stringResource(Res.string.quantity_change_by),
                enabled = state.canUserEdit,
                modifier = Modifier.padding(8.dp)
            )
        }

        item {
            CustomExposedDropdownMenu(
                label = stringResource(Res.string.reason),
                items = StockAdjustmentReason.entries,
                currentSelection = stringResource(state.adjustmentReason.getStringResource()),
                onItemSelected = {
                    onEvent(
                        StockManagementContract.Event.AdjustmentReasonChanged(
                            it
                        )
                    )
                },
                itemToDisplayString = { stringResource(it.getStringResource()) },
                enabled = state.canUserEdit,
                modifier = Modifier.padding(8.dp)
            )
        }

        item {
            LabeledTextField(
                value = state.adjustmentNotes,
                onValueChange = {
                    onEvent(
                        StockManagementContract.Event.AdjustmentNotesChanged(
                            it
                        )
                    )
                },
                label = stringResource(Res.string.notes),
                enabled = state.canUserEdit,
                modifier = Modifier.padding(8.dp)
            )
        }

        item {
            ConfirmDeleteDialog(
                onConfirm = { onEvent(StockManagementContract.Event.DeleteConfirmed) },
                onDismiss = { onEvent(StockManagementContract.Event.DeleteCanceled) },
                show = state.showDeleteDialog
            )
        }
    }
}
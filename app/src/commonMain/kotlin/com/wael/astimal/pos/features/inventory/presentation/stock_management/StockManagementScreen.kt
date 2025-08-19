package com.wael.astimal.pos.features.inventory.presentation.stock_management

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.domain.entity.displayName
import com.wael.astimal.pos.core.presentation.compoenents.ConfirmDeleteDialog
import com.wael.astimal.pos.core.presentation.compoenents.ExposedDropdownMenu
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
fun StockManagementRoute(
    onBack: () -> Unit,
    viewModel: StockManagementViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    StockManagementScreen(
        onBack = onBack,
        state = state, onEvent = viewModel::processEvent
    )
}

@Composable
fun StockManagementScreen(
    state: StockManagementReducer.State,
    onEvent: (StockManagementReducer.Event) -> Unit,
    onBack: () -> Unit,
) {

    val language = LocalAppLocale.current
    SearchScreen(
        query = state.query,
        isSearchActive = state.isSearchActive,
        onQueryChange = { onEvent(StockManagementReducer.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(StockManagementReducer.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(StockManagementReducer.Event.SearchActiveChanged(it)) },
        onBack = onBack,
        lastModifiedDate = state.selectedAdjustment?.updatedAt,
        onDelete = { onEvent(StockManagementReducer.Event.DeleteClicked) },
        onCreate = { onEvent(StockManagementReducer.Event.SaveClicked) },
        onUpdate = { onEvent(StockManagementReducer.Event.SaveClicked) },
        onNew = { onEvent(StockManagementReducer.Event.NewStockAdjustmentClicked) },
        enableFab = state.canSave,
        searchResults = {
            ItemGrid(
                list = state.filterStockAdjustments,
                onItemClick = { stock ->
                    onEvent(
                        StockManagementReducer.Event.SelectedAdjustmentChanged(
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
        ExposedDropdownMenu(
                label = stringResource(Res.string.store),
            options = state.stores.map { it.name.displayName(language) },
            initialText = state.adjustmentStore?.name.displayName(language),
                onItemSelected = {
                    onEvent(
                        StockManagementReducer.Event.AdjustmentStoreChanged(
                            it?.let { state.stores.getOrNull(it) }
                        )
                    )
                },
                enabled = state.canUserEdit,
            )
        ExposedDropdownMenu(
                label = stringResource(Res.string.product),
            options = state.products.map { it.name.displayName(language) },
            initialText = state.adjustmentProduct?.name?.displayName(
                    LocalAppLocale.current
                ) ?: "",
                onItemSelected = {
                    onEvent(
                        StockManagementReducer.Event.AdjustmentProductChanged(
                            it?.let { state.products.getOrNull(it) }
                        )
                    )
                },
                enabled = state.canUserEdit,
            )
            LabeledTextField(
                value = state.adjustmentQuantityChange,
                onValueChange = {
                    onEvent(
                        StockManagementReducer.Event.AdjustmentQuantityChanged(
                            it
                        )
                    )
                },
                label = stringResource(Res.string.quantity_change_by),
                enabled = state.canUserEdit,
            )
        ExposedDropdownMenu(
                label = stringResource(Res.string.reason),
            options = StockAdjustmentReason.entries.map { stringResource(it.getStringResource()) },
            initialText = state.adjustmentReason?.getStringResource()?.let { stringResource(it) }
                ?: "",
                onItemSelected = {
                    onEvent(
                        StockManagementReducer.Event.AdjustmentReasonChanged(
                            it?.let { StockAdjustmentReason.entries.getOrNull(it) }
                        )
                    )
                },
                enabled = state.canUserEdit,
            )
            LabeledTextField(
                value = state.adjustmentNotes,
                onValueChange = {
                    onEvent(
                        StockManagementReducer.Event.AdjustmentNotesChanged(
                            it
                        )
                    )
                },
                label = stringResource(Res.string.notes),
                enabled = state.canUserEdit,
            )

            ConfirmDeleteDialog(
                onConfirm = { onEvent(StockManagementReducer.Event.DeleteConfirmed) },
                onDismiss = { onEvent(StockManagementReducer.Event.DeleteCanceled) },
                show = state.showDeleteDialog
            )
    }
}
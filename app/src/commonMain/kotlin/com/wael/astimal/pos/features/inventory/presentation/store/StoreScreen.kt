package com.wael.astimal.pos.features.inventory.presentation.store

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.domain.entity.displayName
import com.wael.astimal.pos.core.presentation.compoenents.ConfirmDeleteDialog
import com.wael.astimal.pos.core.presentation.compoenents.ExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.inventory.data.local.entity.StoreType
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.address
import pos.app.generated.resources.ar_name
import pos.app.generated.resources.en_name
import pos.app.generated.resources.responsible_employee
import pos.app.generated.resources.store_type
import pos.app.generated.resources.stores

@Composable
fun StoreRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StoreViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    StoreScreen(
        onBack = onBack,
        state = state, onEvent = viewModel::processEvent, modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    state: StoreReducer.State,
    onEvent: (StoreReducer.Event) -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val language = LocalAppLocale.current
    SearchScreen(
        screenTitle = stringResource(Res.string.stores),
        modifier = modifier,
        loading = state.isLoading,
        query = state.searchQuery,
        isSearchActive = state.isSearchActive,
        isNew = !state.isEditing,
        onQueryChange = { onEvent(StoreReducer.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(StoreReducer.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(StoreReducer.Event.SearchActiveChanged(it)) },
        onBack = onBack,
        lastModifiedDate = state.selectedStore?.updatedAt,
        onDelete = { onEvent(StoreReducer.Event.DeleteClicked) },
        onCreate = { onEvent(StoreReducer.Event.SaveClicked) },
        onUpdate = { onEvent(StoreReducer.Event.SaveClicked) },
        onNew = { onEvent(StoreReducer.Event.NewStoreClicked) },
        enableFab = state.enabledFab,
        canUpdate = state.canUpdate,
        canCreate = state.canCreate,
        canDelete = state.canDelete,
        searchResults = {
            ItemGrid(
                list = state.stores,
                onItemClick = { store ->
                    onEvent(StoreReducer.Event.StoreSelected(store))
                },
                label = { Label(it.name.displayName(LocalAppLocale.current)) },
                isSelected = { store -> store.id == state.selectedStore?.id },
            )
        },
        mainContent = {
            LabeledTextField(
                value = state.inputArName,
                onValueChange = { onEvent(StoreReducer.Event.ArNameChanged(it)) },
                label = stringResource(Res.string.ar_name),
                enabled = state.canEdit,
            )
            LabeledTextField(
                value = state.inputEnName,
                onValueChange = { onEvent(StoreReducer.Event.EnNameChanged(it)) },
                label = stringResource(Res.string.en_name),
                enabled = state.canEdit,
            )
            LabeledTextField(
                value = state.inputAddress,
                onValueChange = { onEvent(StoreReducer.Event.AddressChanged(it)) },
                label = stringResource(Res.string.address),
                enabled = state.canEdit,
            )
            ExposedDropdownMenu(
                label = stringResource(Res.string.store_type),
                options = StoreType.entries.map { stringResource(it.getStringResourceId()) },
                onItemSelected = {
                    onEvent(StoreReducer.Event.TypeChanged(it?.let {
                        StoreType.entries.getOrNull(
                            it
                        )
                    }))
                },
                enabled = state.canEdit,
                initialText = state.inputType?.getStringResourceId()?.let {
                    stringResource(it)
                } ?: "",
            )
            ExposedDropdownMenu(
                label = stringResource(Res.string.responsible_employee),
                options = state.employees.map { it.localizedName.displayName(language) },
                onItemSelected = {
                    onEvent(StoreReducer.Event.EmployeeSelected(it?.let {
                        state.employees.getOrNull(
                            it
                        )
                    }))
                },
                initialText = state.selectedEmployee?.localizedName.displayName(language),
                enabled = state.canEdit,
                imeAction = ImeAction.Done,
            )
            ConfirmDeleteDialog(
                onConfirm = { onEvent(StoreReducer.Event.DeleteConfirmed) },
                onDismiss = { onEvent(StoreReducer.Event.DeleteCanceled) },
                show = state.showDeleteDialog
            )
        },
    )
}

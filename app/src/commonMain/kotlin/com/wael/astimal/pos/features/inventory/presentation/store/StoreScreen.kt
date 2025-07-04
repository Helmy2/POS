package com.wael.astimal.pos.features.inventory.presentation.store

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.inventory.data.entity.StoreType
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.ar_name
import pos.app.generated.resources.en_name
import pos.app.generated.resources.store_type

@Composable
fun StoreRoute(
    modifier: Modifier = Modifier,
    viewModel: StoreViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    StoreScreen(
        state = state, onEvent = viewModel::processEvent, modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    state: StoreContract.State,
    onEvent: (StoreContract.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchScreen(
        modifier = modifier,
        query = state.searchQuery,
        isSearchActive = state.isSearchActive,
        isNew = !state.isEditing,
        onQueryChange = { onEvent(StoreContract.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(StoreContract.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(StoreContract.Event.SearchActiveChanged(it)) },
        onBack = { onEvent(StoreContract.Event.BackClicked) },
        lastModifiedDate = state.selectedStore?.updatedAt,
        onDelete = { onEvent(StoreContract.Event.DeleteClicked) },
        onCreate = { onEvent(StoreContract.Event.SaveClicked) },
        onUpdate = { onEvent(StoreContract.Event.SaveClicked) },
        onNew = { onEvent(StoreContract.Event.NewStoreClicked) },
        canEdit = state.canUserEdit,
        canSave = state.canSave,
        searchResults = {
            ItemGrid(
                list = state.stores,
                onItemClick = { store ->
                    onEvent(StoreContract.Event.StoreSelected(store))
                },
                label = { Label(it.name.displayName(LocalAppLocale.current)) },
                isSelected = { store -> store.id.local == state.selectedStore?.id?.local },
            )
        },
        mainContent = {
            item {
                LabeledTextField(
                    value = state.inputArName,
                    onValueChange = { onEvent(StoreContract.Event.ArNameChanged(it)) },
                    label = stringResource(Res.string.ar_name),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)
                )
            }
            item {
                LabeledTextField(
                    value = state.inputEnName,
                    onValueChange = { onEvent(StoreContract.Event.EnNameChanged(it)) },
                    label = stringResource(Res.string.en_name),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)
                )
            }
            item {
                CustomExposedDropdownMenu(
                    label = stringResource(Res.string.store_type),
                    currentSelection = stringResource(state.inputType.getStringResourceId()),
                    items = StoreType.entries,
                    onItemSelected = { onEvent(StoreContract.Event.TypeChanged(it)) },
                    enabled = state.canUserEdit,
                    itemToDisplayString = { stringResource(it.getStringResourceId()) },
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
    )
}

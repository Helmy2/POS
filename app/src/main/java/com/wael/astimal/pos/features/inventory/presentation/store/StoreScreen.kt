package com.wael.astimal.pos.features.inventory.presentation.store

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.inventory.data.entity.StoreType
import com.wael.astimal.pos.core.presentation.compoenents.CustomExposedDropdownMenu
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import org.koin.androidx.compose.koinViewModel


@Composable
fun StoreRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StoreViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    StoreScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    state: StoreState,
    onEvent: (StoreEvent) -> Unit,
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
        onQueryChange = { onEvent(StoreEvent.UpdateQuery(it)) },
        onSearch = { onEvent(StoreEvent.Search(it)) },
        onSearchActiveChange = { onEvent(StoreEvent.UpdateIsQueryActive(it)) },
        onBack = onBack,
        lastModifiedDate = state.selectedStore?.lastModified,
        onDelete = { onEvent(StoreEvent.DeleteStore) },
        onCreate = { onEvent(StoreEvent.CreateStore) },
        onUpdate = { onEvent(StoreEvent.UpdateStore) },
        onNew = { onEvent(StoreEvent.SelectStore(null)) },
        searchResults = {
            ItemGrid(
                list = state.searchResults,
                onItemClick = { store ->
                    onEvent(StoreEvent.UpdateIsQueryActive(false))
                    onEvent(StoreEvent.SelectStore(store))
                },
                label = { Text(it.name.displayName(language)) },
                isSelected = { store -> store.localId == state.selectedStore?.localId },
            )
        },
        mainContent = {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (state.error != null) {
                    Text(state.error, color = MaterialTheme.colorScheme.error)
                }
                LabeledTextField(
                    value = state.inputArName,
                    onValueChange = { onEvent(StoreEvent.UpdateInputArName(it)) },
                    label = stringResource(id = R.string.ar_name),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                LabeledTextField(
                    value = state.inputEnName,
                    onValueChange = { onEvent(StoreEvent.UpdateInputEnName(it)) },
                    label = stringResource(id = R.string.en_name),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                CustomExposedDropdownMenu(
                    label = stringResource(id = R.string.store_type),
                    items = StoreType.entries,
                    selectedItemId = state.inputType?.ordinal?.toLong(),
                    onItemSelected = { onEvent(StoreEvent.UpdateInputType(it)) },
                    itemToDisplayString = { it.name },
                    itemToId = { it.ordinal.toLong() }
                )
            }
        },
    )
}



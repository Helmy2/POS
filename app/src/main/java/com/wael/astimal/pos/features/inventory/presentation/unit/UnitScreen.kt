package com.wael.astimal.pos.features.inventory.presentation.unit

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import org.koin.androidx.compose.koinViewModel

@Composable
fun UnitRoute(
    modifier: Modifier = Modifier,
    viewModel: UnitViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    UnitScreen(
        state = state,
        onEvent = viewModel::processEvent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitScreen(
    state: UnitContract.State,
    onEvent: (UnitContract.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppLocale.current

    SearchScreen(
        modifier = modifier,
        query = state.searchQuery,
        isSearchActive = state.isSearchActive,
        isNew = !state.isEditing,
        onQueryChange = { onEvent(UnitContract.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(UnitContract.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(UnitContract.Event.SearchActiveChanged(it)) },
        onBack = { onEvent(UnitContract.Event.BackClicked) },
        lastModifiedDate = state.selectedUnit?.updatedAt,
        onDelete = { onEvent(UnitContract.Event.DeleteClicked) },
        onCreate = { onEvent(UnitContract.Event.SaveClicked) },
        onUpdate = { onEvent(UnitContract.Event.SaveClicked) },
        onNew = { onEvent(UnitContract.Event.NewUnitClicked) },
        canEdit = state.canUserEdit,
        canSave = state.canSave,
        searchResults = {
            ItemGrid(
                list = state.units,
                onItemClick = { unit -> onEvent(UnitContract.Event.UnitSelected(unit)) },
                label = { Label(it.name.displayName(language)) },
                isSelected = { unit -> unit.id.local == state.selectedUnit?.id?.local },
            )
        },
        mainContent = {
            item {
                LabeledTextField(
                    value = state.inputEnName,
                    onValueChange = { onEvent(UnitContract.Event.EnNameChanged(it)) },
                    label = stringResource(id = R.string.en_name),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)
                )
            }
            item {
                LabeledTextField(
                    value = state.inputArName,
                    onValueChange = { onEvent(UnitContract.Event.ArNameChanged(it)) },
                    label = stringResource(id = R.string.ar_name),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
    )
}

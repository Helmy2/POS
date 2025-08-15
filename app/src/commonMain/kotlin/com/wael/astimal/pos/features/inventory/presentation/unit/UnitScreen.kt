package com.wael.astimal.pos.features.inventory.presentation.unit

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.presentation.compoenents.ConfirmDeleteDialog
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.ar_abbreviation
import pos.app.generated.resources.ar_name
import pos.app.generated.resources.en_abbreviation
import pos.app.generated.resources.en_name

@Composable
fun UnitRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UnitViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    UnitScreen(
        onBack = onBack, state = state, onEvent = viewModel::processEvent, modifier = modifier
    )
}

@Composable
fun UnitScreen(
    state: UnitReducer.State,
    onEvent: (UnitReducer.Event) -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val language = LocalAppLocale.current

    SearchScreen(
        modifier = modifier,
        query = state.searchQuery,
        isSearchActive = state.isSearchActive,
        isNew = !state.isEditing,
        onQueryChange = { onEvent(UnitReducer.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(UnitReducer.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(UnitReducer.Event.SearchActiveChanged(it)) },
        onBack = onBack,
        lastModifiedDate = state.selectedUnit?.updatedAt,
        onDelete = { onEvent(UnitReducer.Event.DeleteClicked) },
        onCreate = { onEvent(UnitReducer.Event.SaveClicked) },
        onUpdate = { onEvent(UnitReducer.Event.SaveClicked) },
        onNew = { onEvent(UnitReducer.Event.NewUnitClicked) },
        canEdit = state.canUserEdit,
        canSave = state.canSave,
        searchResults = {
            ItemGrid(
                list = state.units,
                onItemClick = { unit -> onEvent(UnitReducer.Event.UnitSelected(unit)) },
                label = { Label(it.name.displayName(language)) },
                isSelected = { unit -> unit.id == state.selectedUnit?.id },
            )
        },
        mainContent = {
            LabeledTextField(
                value = state.inputEnName,
                onValueChange = { onEvent(UnitReducer.Event.EnNameChanged(it)) },
                label = stringResource(Res.string.en_name),
                enabled = state.canUserEdit,
            )
            LabeledTextField(
                value = state.inputEnAbbreviation,
                onValueChange = { onEvent(UnitReducer.Event.EnAbbreviationChanged(it)) },
                label = stringResource(Res.string.en_abbreviation),
                enabled = state.canUserEdit,
            )
            LabeledTextField(
                value = state.inputArName,
                onValueChange = { onEvent(UnitReducer.Event.ArNameChanged(it)) },
                label = stringResource(Res.string.ar_name),
                enabled = state.canUserEdit,
            )
            LabeledTextField(
                value = state.inputArAbbreviation,
                onValueChange = { onEvent(UnitReducer.Event.ArAbbreviationChanged(it)) },
                label = stringResource(Res.string.ar_abbreviation),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                enabled = state.canUserEdit,
            )
            ConfirmDeleteDialog(
                onConfirm = { onEvent(UnitReducer.Event.DeleteConfirmed) },
                onDismiss = { onEvent(UnitReducer.Event.DeleteCanceled) },
                show = state.showDeleteDialog
            )
        },
    )
}

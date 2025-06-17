package com.wael.astimal.pos.features.inventory.presentation.unit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import com.wael.astimal.pos.core.presentation.snackbar.UiEvent
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import kotlinx.coroutines.flow.SharedFlow
import org.koin.androidx.compose.koinViewModel

@Composable
fun UnitRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UnitViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    UnitScreen(
        state = state, onEvent = viewModel::handleEvent,
        onBack = onBack,
        modifier,
        eventFlow = viewModel.eventFlow,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UnitScreen(
    state: UnitDetailsState,
    onEvent: (UnitEvent) -> Unit,
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
        onQueryChange = { onEvent(UnitEvent.UpdateQuery(it)) },
        onSearch = { onEvent(UnitEvent.Search(it)) },
        onSearchActiveChange = { onEvent(UnitEvent.UpdateIsQueryActive(it)) },
        onBack = onBack,
        lastModifiedDate = state.selectedProductUnit?.updatedAt,
        onDelete = { onEvent(UnitEvent.DeleteUnit) },
        onCreate = { onEvent(UnitEvent.CreateUnit) },
        onUpdate = { onEvent(UnitEvent.UpdateUnit) },
        onNew = { onEvent(UnitEvent.NewUnit) },
        canEdit = state.canEdit,
        searchResults = {
            ItemGrid(
                list = state.searchResults,
                onItemClick = {
                    onEvent(UnitEvent.UpdateIsQueryActive(false))
                    onEvent(UnitEvent.Select(it))
                },
                label = { Label(it.localizedName.displayName(language)) },
                isSelected = { it.id.local == state.selectedProductUnit?.id?.local },
            )
        },
        mainContent = {
            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LabeledTextField(
                    value = state.arName,
                    onValueChange = { onEvent(UnitEvent.UpdateArName(it)) },
                    label = stringResource(R.string.ar_name),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    enabled = state.canEdit,
                )
                LabeledTextField(
                    value = state.enName,
                    onValueChange = { onEvent(UnitEvent.UpdateArName(it)) },
                    label = stringResource(R.string.en_name),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    enabled = state.canEdit,
                )
            }
        },
    )
}




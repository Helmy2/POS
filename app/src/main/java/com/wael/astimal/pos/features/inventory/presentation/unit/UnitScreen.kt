package com.wael.astimal.pos.features.inventory.presentation.unit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import com.wael.astimal.pos.features.inventory.presentation.components.ItemGrid
import com.wael.astimal.pos.features.inventory.presentation.components.LabeledTextField
import com.wael.astimal.pos.features.inventory.presentation.components.SearchScreen
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
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UnitScreen(
    state: UnitDetailsState,
    onEvent: (UnitEvent) -> Unit,
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
        onQueryChange = { onEvent(UnitEvent.UpdateQuery(it)) },
        onSearch = { onEvent(UnitEvent.Search(it)) },
        onSearchActiveChange = { onEvent(UnitEvent.UpdateIsQueryActive(it)) },
        onBack = onBack,
        onDelete = { onEvent(UnitEvent.DeleteUnit) },
        onCreate = { onEvent(UnitEvent.CreateUnit) },
        onUpdate = { onEvent(UnitEvent.UpdateUnit) },
        onNew = { onEvent(UnitEvent.NewUnit) },
        searchResults = {
            ItemGrid(
                list = state.searchResults,
                onItemClick = {
                    onEvent(UnitEvent.UpdateIsQueryActive(false))
                    onEvent(UnitEvent.Select(it))
                },
                labelProvider = { it.localizedName.displayName(language) },
                isSelected = { it.localId == state.selectedUnit?.localId },
            )
        },
        mainContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FlowRow {
                    LabeledTextField(
                        value = state.arName,
                        onValueChange = { onEvent(UnitEvent.UpdateArName(it)) },
                        label = stringResource(R.string.ar_name),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    LabeledTextField(
                        value = state.enName,
                        onValueChange = { onEvent(UnitEvent.UpdateArName(it)) },
                        label = stringResource(R.string.en_name),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                }
                LabeledTextField(
                    value = state.rate,
                    onValueChange = {
                        if (it.toFloatOrNull() != null || it.isBlank())
                            onEvent(UnitEvent.UpdateRate(it))
                    },
                    label = stringResource(R.string.rate),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Number
                    )
                )
            }
        },
    )
}




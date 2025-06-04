package com.wael.astimal.pos.features.inventory.presentation.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import com.wael.astimal.pos.features.inventory.presentation.components.ItemGrid
import com.wael.astimal.pos.features.inventory.presentation.components.LabeledTextField
import com.wael.astimal.pos.features.inventory.presentation.components.SearchScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun CategoryRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CategoryScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryScreen(
    state: CategoryScreenState,
    onEvent: (CategoryScreenEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchScreen(
        modifier = modifier,
        query = state.query,
        isSearchActive = state.isQueryActive,
        loading = state.loading,
        isNew = state.isNew,
        onQueryChange = { onEvent(CategoryScreenEvent.UpdateQuery(it)) },
        onSearch = { onEvent(CategoryScreenEvent.Search(it)) },
        onSearchActiveChange = { onEvent(CategoryScreenEvent.UpdateIsQueryActive(it)) },
        onBack = onBack,
        onDelete = { onEvent(CategoryScreenEvent.DeleteCategory) },
        onCreate = { onEvent(CategoryScreenEvent.CreateCategory) },
        onUpdate = { onEvent(CategoryScreenEvent.UpdateCategory) },
        onNew = { onEvent(CategoryScreenEvent.SelectCategory(null)) },
        searchResults = {
            ItemGrid(
                list = state.searchResults,
                onItemClick = { category ->
                    onEvent(CategoryScreenEvent.UpdateIsQueryActive(false))
                    onEvent(CategoryScreenEvent.SelectCategory(category))
                },
                labelProvider = { "${it.enName}: ${it.arName}" },
                isSelected = { category -> category.localId == state.selectedCategory?.localId },
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
                    onValueChange = { onEvent(CategoryScreenEvent.UpdateInputArName(it)) },
                    label = stringResource(id = R.string.ar_name),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                LabeledTextField(
                    value = state.inputEnName,
                    onValueChange = { onEvent(CategoryScreenEvent.UpdateInputEnName(it)) },
                    label = stringResource(id = R.string.en_name),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
            }
        },
    )
}
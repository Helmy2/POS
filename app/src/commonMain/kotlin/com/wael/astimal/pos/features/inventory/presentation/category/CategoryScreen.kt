package com.wael.astimal.pos.features.inventory.presentation.category

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.presentation.compoenents.ItemGrid
import com.wael.astimal.pos.core.presentation.compoenents.Label
import com.wael.astimal.pos.core.presentation.compoenents.LabeledTextField
import com.wael.astimal.pos.core.presentation.compoenents.SearchScreen
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.ar_name
import pos.app.generated.resources.en_name

@Composable
fun CategoryRoute(
    modifier: Modifier = Modifier,
    viewModel: CategoryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CategoryScreen(
        state = state,
        onEvent = viewModel::processEvent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    state: CategoryContract.State,
    onEvent: (CategoryContract.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = LocalAppLocale.current

    SearchScreen(
        modifier = modifier,
        query = state.searchQuery,
        isSearchActive = state.isSearchActive,
        isNew = !state.isEditing,
        onQueryChange = { onEvent(CategoryContract.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(CategoryContract.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(CategoryContract.Event.SearchActiveChanged(it)) },
        onBack = { onEvent(CategoryContract.Event.BackClicked) },
        lastModifiedDate = state.selectedCategory?.updatedAt,
        onDelete = { onEvent(CategoryContract.Event.DeleteClicked) },
        onCreate = { onEvent(CategoryContract.Event.SaveClicked) },
        onUpdate = { onEvent(CategoryContract.Event.SaveClicked) },
        onNew = { onEvent(CategoryContract.Event.NewCategoryClicked) },
        canEdit = state.canUserEdit,
        canSave = state.canSave,
        searchResults = {
            ItemGrid(
                list = state.categories,
                onItemClick = { category -> onEvent(CategoryContract.Event.CategorySelected(category)) },
                label = { Label(it.name.displayName(language)) },
                isSelected = { category -> category.id.local == state.selectedCategory?.id?.local },
            )
        },
        mainContent = {
            item {
                LabeledTextField(
                    value = state.inputEnName,
                    onValueChange = { onEvent(CategoryContract.Event.EnNameChanged(it)) },
                    label = stringResource(Res.string.en_name),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)
                )
            }
            item {
                LabeledTextField(
                    value = state.inputArName,
                    onValueChange = { onEvent(CategoryContract.Event.ArNameChanged(it)) },
                    label = stringResource(Res.string.ar_name),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    enabled = state.canUserEdit,
                    modifier = Modifier.padding(8.dp)
                )
            }
        },
    )
}

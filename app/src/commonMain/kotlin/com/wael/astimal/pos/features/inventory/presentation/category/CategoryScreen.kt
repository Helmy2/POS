package com.wael.astimal.pos.features.inventory.presentation.category

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
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
import pos.app.generated.resources.ar_name
import pos.app.generated.resources.en_name

@Composable
fun CategoryRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CategoryScreen(
        state = state,
        onEvent = viewModel::processEvent,
        onBack = onBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    state: CategoryReducer.State,
    onEvent: (CategoryReducer.Event) -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val language = LocalAppLocale.current

    SearchScreen(
        modifier = modifier,
        loading = state.isLoading,
        query = state.searchQuery,
        isSearchActive = state.isSearchActive,
        isNew = !state.isEditing,
        onQueryChange = { onEvent(CategoryReducer.Event.SearchQueryChanged(it)) },
        onSearch = { onEvent(CategoryReducer.Event.SearchQueryChanged(it)) },
        onSearchActiveChange = { onEvent(CategoryReducer.Event.SearchActiveChanged(it)) },
        onBack = onBack,
        lastModifiedDate = state.selectedCategory?.updatedAt,
        onDelete = { onEvent(CategoryReducer.Event.DeleteClicked) },
        onCreate = { onEvent(CategoryReducer.Event.SaveClicked) },
        onUpdate = { onEvent(CategoryReducer.Event.SaveClicked) },
        onNew = { onEvent(CategoryReducer.Event.NewCategoryClicked) },
        enableFab = state.enabledFab,
        canDelete = state.canDelete,
        canCreate = state.canCreate,
        canUpdate = state.canUpdate,
        searchResults = {
            ItemGrid(
                list = state.categories,
                onItemClick = { category -> onEvent(CategoryReducer.Event.CategorySelected(category)) },
                label = { Label(it.name.displayName(language)) },
                isSelected = { category -> category.id == state.selectedCategory?.id },
            )
        },
        mainContent = {
            LabeledTextField(
                value = state.inputEnName,
                onValueChange = { onEvent(CategoryReducer.Event.EnNameChanged(it)) },
                label = stringResource(Res.string.en_name),
                enabled = state.canEdit,
            )
            LabeledTextField(
                value = state.inputArName,
                onValueChange = { onEvent(CategoryReducer.Event.ArNameChanged(it)) },
                label = stringResource(Res.string.ar_name),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                enabled = state.canEdit,
            )
            ConfirmDeleteDialog(
                onConfirm = { onEvent(CategoryReducer.Event.DeleteConfirmed) },
                onDismiss = { onEvent(CategoryReducer.Event.DeleteCanceled) },
                show = state.showDeleteDialog
            )
        },
    )
}

package com.wael.astimal.pos.features.inventory.presentation.category

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.entity.Category
import com.wael.astimal.pos.features.inventory.domain.repository.CategoryRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pos.app.generated.resources.Res
import pos.app.generated.resources.category_deleted_successfully
import pos.app.generated.resources.category_saved_successfully
import pos.app.generated.resources.error_some_field_are_required
import pos.app.generated.resources.failed_to_delete_category
import pos.app.generated.resources.failed_to_load_categories
import pos.app.generated.resources.failed_to_save_category

class CategoryViewModel(
    private val categoryRepository: CategoryRepository,
    private val userRepository: UserRepository,
    private val snackbarController: SnackbarController,
) : BaseViewModel<CategoryReducer.State, CategoryReducer.Event, Nothing>(
    reducer = CategoryReducer,
    initialState = CategoryReducer.State()
) {
    private var searchJob: Job? = null

    init {
        loadCurrentUser()
        searchCategories("")
    }

    override fun handleEvent(event: CategoryReducer.Event) {
        when (event) {
            is CategoryReducer.Event.SearchQueryChanged -> {
                setState(event)
                searchCategories(event.query)
            }

            is CategoryReducer.Event.SaveClicked -> saveCategory()
            is CategoryReducer.Event.DeleteConfirmed -> deleteCategory()
            else -> setState(event)
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            setState(CategoryReducer.Event.UserLoaded(userRepository.getCurrentUser()))
        }
    }

    @OptIn(FlowPreview::class)
    private fun searchCategories(query: String) {
        searchJob?.cancel()
        setState(CategoryReducer.Event.LoadingStarted)
        searchJob = categoryRepository.getCategories(query)
            .debounce(300L)
            .catch {
                setState(CategoryReducer.Event.LoadingFinished)
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_load_categories)))
            }
            .onEach { categories ->
                setState(CategoryReducer.Event.CategoriesLoaded(categories))
            }
            .launchIn(viewModelScope)
    }

    private fun saveCategory() {
        viewModelScope.launch {
            val currentState = state.value
            if (!currentState.canSave) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_some_field_are_required)))
                return@launch
            }
            setState(CategoryReducer.Event.LoadingStarted)
            viewModelScope.launch {
                val categoryToSave = Category(
                    id = currentState.selectedCategory?.id ?: "",
                    name = LocalizedString(
                        arName = currentState.inputArName,
                        enName = currentState.inputEnName
                    ),
                    createdAt = currentState.selectedCategory?.createdAt ?: Clock.now(),
                )

                val result = categoryRepository.saveCategory(categoryToSave)

                result.onSuccess {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.category_saved_successfully)))
                    setState(CategoryReducer.Event.SaveSucceeded)
                    searchCategories("") // Refresh the list
                }.onFailure {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_save_category)))
                    setState(CategoryReducer.Event.LoadingFinished)
                }
            }
        }
    }

    private fun deleteCategory() {
        setState(CategoryReducer.Event.DeleteConfirmed)
        val categoryToDelete = state.value.selectedCategory ?: return
        setState(CategoryReducer.Event.LoadingStarted)
        viewModelScope.launch {
            categoryRepository.deleteCategory(categoryToDelete).onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.category_deleted_successfully)))
                setState(CategoryReducer.Event.DeleteSucceeded)
                searchCategories("") // Refresh the list
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_delete_category)))
                setState(CategoryReducer.Event.LoadingFinished)
            }
        }
    }
}
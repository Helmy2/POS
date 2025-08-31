package com.wael.astimal.pos.features.inventory.presentation.category

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.core.util.SHOULD_SHOW_SHEATH_ON_START
import com.wael.astimal.pos.features.inventory.domain.entity.Category
import com.wael.astimal.pos.features.user.domain.PermissionManager
import com.wael.astimal.pos.features.user.domain.entity.User

object CategoryReducer : Reducer<CategoryReducer.State, CategoryReducer.Event, Nothing> {
    data class State(
        val isLoading: Boolean = false,
        val categories: List<Category> = emptyList(),
        val selectedCategory: Category? = null,
        val searchQuery: String = "",
        val isSearchActive: Boolean = SHOULD_SHOW_SHEATH_ON_START,
        val currentUser: User? = null,
        val inputArName: String = "",
        val inputEnName: String = "",
        val showDeleteDialog: Boolean = false
    ) : Reducer.ViewState {
        val isEditing: Boolean get() = selectedCategory != null

        val enabledFab: Boolean get() = inputArName.isNotBlank()
        val canCreate: Boolean get() = PermissionManager.canCreate(Destination.Categories)
        val canUpdate: Boolean get() = PermissionManager.canUpdate(Destination.Categories)
        val canDelete: Boolean get() = PermissionManager.canDelete(Destination.Categories)
        val canEdit: Boolean get() = canCreate && !isEditing || canUpdate && isEditing
    }

    sealed interface Event : Reducer.ViewEvent {
        // UI Actions
        data class SearchQueryChanged(val query: String) : Event
        data class SearchActiveChanged(val isActive: Boolean) : Event
        data class CategorySelected(val category: Category) : Event
        data object NewCategoryClicked : Event
        data object SaveClicked : Event
        data object DeleteClicked : Event
        data object DeleteConfirmed : Event
        data object DeleteCanceled : Event

        // Form Input Changes
        data class ArNameChanged(val name: String) : Event
        data class EnNameChanged(val name: String) : Event

        // Data results from ViewModel
        data class UserLoaded(val user: User?) : Event
        data class CategoriesLoaded(val categories: List<Category>) : Event
        data object SaveSucceeded : Event
        data object DeleteSucceeded : Event
        data object LoadingStarted : Event
        data object LoadingFinished : Event
    }

    override fun reduce(
        previousState: State, event: Event
    ): Pair<State, Nothing?> {
        return when (event) {
            is Event.LoadingStarted -> previousState.copy(isLoading = true) to null

            is Event.LoadingFinished -> previousState.copy(isLoading = false) to null

            is Event.SearchQueryChanged -> previousState.copy(searchQuery = event.query) to null

            is Event.SearchActiveChanged -> previousState.copy(isSearchActive = event.isActive) to null

            is Event.UserLoaded -> previousState.copy(currentUser = event.user) to null

            is Event.CategoriesLoaded -> previousState.copy(
                isLoading = false, categories = event.categories
            ) to null

            is Event.CategorySelected -> previousState.copy(
                selectedCategory = event.category,
                inputArName = event.category.name.arName ?: "",
                inputEnName = event.category.name.enName ?: "",
                isSearchActive = false
            ) to null

            is Event.NewCategoryClicked, is Event.SaveSucceeded, is Event.DeleteSucceeded ->
                // Clear the form
                previousState.copy(
                    isLoading = false, selectedCategory = null, inputArName = "", inputEnName = ""
                ) to null

            // Form input updates
            is Event.ArNameChanged -> previousState.copy(inputArName = event.name) to null

            is Event.EnNameChanged -> previousState.copy(inputEnName = event.name) to null

            is Event.DeleteClicked -> previousState.copy(showDeleteDialog = true) to null

            is Event.DeleteCanceled, is Event.DeleteConfirmed -> previousState.copy(
                showDeleteDialog = false
            ) to null

            // Events that trigger sagas in ViewModel but don't change state directly
            is Event.SaveClicked -> previousState to null
        }
    }
}

package com.wael.astimal.pos.features.inventory.presentation.category

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.inventory.domain.entity.Category
import com.wael.astimal.pos.features.user.domain.entity.User

object CategoryContract {

    data class State(
        val isLoading: Boolean = false,
        val categories: List<Category> = emptyList(),
        val selectedCategory: Category? = null,
        val searchQuery: String = "",
        val isSearchActive: Boolean = false,
        val currentUser: User? = null,
        val inputArName: String = "",
        val inputEnName: String = ""
    ) : Reducer.ViewState {
        val isEditing: Boolean get() = selectedCategory != null
        val canUserEdit: Boolean get() = currentUser?.isAdmin == true
        val canSave: Boolean get() = inputArName.isNotBlank() || inputEnName.isNotBlank()
    }

    sealed interface Event : Reducer.ViewEvent {
        // UI Actions
        data class SearchQueryChanged(val query: String) : Event
        data class SearchActiveChanged(val isActive: Boolean) : Event
        data class CategorySelected(val category: Category) : Event
        data object NewCategoryClicked : Event
        data object SaveClicked : Event
        data object DeleteClicked : Event
        data object BackClicked : Event

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
}

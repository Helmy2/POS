package com.wael.astimal.pos.features.inventory.presentation.category

import com.wael.astimal.pos.core.base.mvi.Reducer

class CategoryReducer : Reducer<CategoryContract.State, CategoryContract.Event, Nothing> {
    override fun reduce(
        previousState: CategoryContract.State,
        event: CategoryContract.Event
    ): Pair<CategoryContract.State, Nothing?> {
        return when (event) {
            is CategoryContract.Event.LoadingStarted ->
                previousState.copy(isLoading = true) to null

            is CategoryContract.Event.LoadingFinished ->
                previousState.copy(isLoading = false) to null

            is CategoryContract.Event.SearchQueryChanged ->
                previousState.copy(searchQuery = event.query) to null

            is CategoryContract.Event.SearchActiveChanged ->
                previousState.copy(isSearchActive = event.isActive) to null

            is CategoryContract.Event.UserLoaded ->
                previousState.copy(currentUser = event.user) to null

            is CategoryContract.Event.CategoriesLoaded ->
                previousState.copy(isLoading = false, categories = event.categories) to null

            is CategoryContract.Event.CategorySelected ->
                previousState.copy(
                    selectedCategory = event.category,
                    inputArName = event.category.name.arName ?: "",
                    inputEnName = event.category.name.enName ?: "",
                    isSearchActive = false
                ) to null

            is CategoryContract.Event.NewCategoryClicked,
            is CategoryContract.Event.SaveSucceeded,
            is CategoryContract.Event.DeleteSucceeded ->
                // Clear the form
                previousState.copy(
                    isLoading = false,
                    selectedCategory = null,
                    inputArName = "",
                    inputEnName = ""
                ) to null

            // Form input updates
            is CategoryContract.Event.ArNameChanged ->
                previousState.copy(inputArName = event.name) to null

            is CategoryContract.Event.EnNameChanged ->
                previousState.copy(inputEnName = event.name) to null

            is CategoryContract.Event.DeleteClicked ->
                previousState.copy(showDeleteDialog = true) to null

            is CategoryContract.Event.DeleteCanceled ->
                previousState.copy(showDeleteDialog = false) to null

            // Events that trigger sagas in ViewModel but don't change state directly
            is CategoryContract.Event.BackClicked,
            is CategoryContract.Event.SaveClicked,
            is CategoryContract.Event.DeleteConfirmed -> previousState to null
        }
    }
}

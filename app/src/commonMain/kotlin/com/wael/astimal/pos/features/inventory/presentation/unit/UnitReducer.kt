package com.wael.astimal.pos.features.inventory.presentation.unit

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.util.SHOULD_SHOW_SHEATH_ON_START
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import com.wael.astimal.pos.features.user.domain.entity.User

class UnitReducer : Reducer<UnitReducer.State, UnitReducer.Event, Nothing> {
    data class State(
        val isLoading: Boolean = false,
        val units: List<ProductUnit> = emptyList(),
        val selectedUnit: ProductUnit? = null,
        val searchQuery: String = "",
        val isSearchActive: Boolean = SHOULD_SHOW_SHEATH_ON_START,
        val currentUser: User? = null,
        // Form input state
        val inputArName: String = "",
        val inputEnName: String = "",
        val inputArAbbreviation: String = "",
        val inputEnAbbreviation: String = "",
        val showDeleteDialog: Boolean = false
    ) : Reducer.ViewState {
        val isEditing: Boolean get() = selectedUnit != null
        val canUserEdit: Boolean get() = currentUser?.isAdmin == true
        val canSave: Boolean
            get() = inputArName.isNotBlank()
    }

    sealed interface Event : Reducer.ViewEvent {
        // UI Actions
        data class SearchQueryChanged(val query: String) : Event
        data class SearchActiveChanged(val isActive: Boolean) : Event
        data class UnitSelected(val unit: ProductUnit) : Event
        data object NewUnitClicked : Event
        data object SaveClicked : Event
        data object DeleteClicked : Event
        data object DeleteConfirmed : Event
        data object DeleteCanceled : Event


        // Form Input Changes
        data class ArNameChanged(val name: String) : Event
        data class EnNameChanged(val name: String) : Event
        data class ArAbbreviationChanged(val name: String) : Event
        data class EnAbbreviationChanged(val name: String) : Event

        // Data results from ViewModel
        data class UserLoaded(val user: User?) : Event
        data class UnitsLoaded(val units: List<ProductUnit>) : Event
        data object SaveSucceeded : Event
        data object DeleteSucceeded : Event
        data object LoadingStarted : Event
        data object LoadingFinished : Event
    }
    override fun reduce(
        previousState: State,
        event: Event
    ): Pair<State, Nothing?> {
        return when (event) {
            is Event.LoadingStarted ->
                previousState.copy(isLoading = true) to null

            is Event.LoadingFinished ->
                previousState.copy(isLoading = false) to null

            is Event.SearchQueryChanged ->
                previousState.copy(searchQuery = event.query) to null

            is Event.SearchActiveChanged ->
                previousState.copy(isSearchActive = event.isActive) to null

            is Event.UserLoaded ->
                previousState.copy(currentUser = event.user) to null

            is Event.UnitsLoaded ->
                previousState.copy(isLoading = false, units = event.units) to null

            is Event.UnitSelected ->
                previousState.copy(
                    selectedUnit = event.unit,
                    inputArName = event.unit.name.arName ?: "",
                    inputEnName = event.unit.name.enName ?: "",
                    inputArAbbreviation = event.unit.abbreviation.arName ?: "",
                    inputEnAbbreviation = event.unit.abbreviation.enName ?: "",
                    isSearchActive = false
                ) to null

            is Event.NewUnitClicked,
            is Event.SaveSucceeded,
            is Event.DeleteSucceeded ->
                // Clear the form
                previousState.copy(
                    isLoading = false,
                    selectedUnit = null,
                    inputArName = "",
                    inputEnName = "",
                    inputArAbbreviation = "",
                    inputEnAbbreviation = ""
                ) to null

            // Form input updates
            is Event.ArNameChanged ->
                previousState.copy(inputArName = event.name) to null

            is Event.EnNameChanged ->
                previousState.copy(inputEnName = event.name) to null

            is Event.ArAbbreviationChanged ->
                previousState.copy(inputArAbbreviation = event.name) to null

            is Event.EnAbbreviationChanged ->
                previousState.copy(inputEnAbbreviation = event.name) to null

            is Event.DeleteClicked -> previousState.copy(
                showDeleteDialog = true
            ) to null

            is Event.DeleteCanceled, is Event.DeleteConfirmed -> previousState.copy(
                showDeleteDialog = false
            ) to null

            // Events that trigger sagas in ViewModel but don't change state directly
            is Event.SaveClicked -> previousState to null
        }
    }
}

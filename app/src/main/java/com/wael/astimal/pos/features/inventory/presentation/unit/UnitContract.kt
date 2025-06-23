package com.wael.astimal.pos.features.inventory.presentation.unit

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import com.wael.astimal.pos.features.user.domain.entity.User

object UnitContract {

    data class State(
        val isLoading: Boolean = false,
        val units: List<ProductUnit> = emptyList(),
        val selectedUnit: ProductUnit? = null,
        val searchQuery: String = "",
        val isSearchActive: Boolean = false,
        val currentUser: User? = null,
        // Form input state
        val inputArName: String = "",
        val inputEnName: String = "",
    ) : Reducer.ViewState {
        val isEditing: Boolean get() = selectedUnit != null
        val canUserEdit: Boolean get() = currentUser?.isAdmin == true
        val canSave: Boolean
            get() = inputEnName.isNotBlank() && inputArName.isNotBlank()
    }

    sealed interface Event : Reducer.ViewEvent {
        // UI Actions
        data class SearchQueryChanged(val query: String) : Event
        data class SearchActiveChanged(val isActive: Boolean) : Event
        data class UnitSelected(val unit: ProductUnit) : Event
        data object NewUnitClicked : Event
        data object SaveClicked : Event
        data object DeleteClicked : Event
        data object BackClicked : Event

        // Form Input Changes
        data class ArNameChanged(val name: String) : Event
        data class EnNameChanged(val name: String) : Event

        // Data results from ViewModel
        data class UserLoaded(val user: User?) : Event
        data class UnitsLoaded(val units: List<ProductUnit>) : Event
        data object SaveSucceeded : Event
        data object DeleteSucceeded : Event
        data object LoadingStarted : Event
        data object LoadingFinished : Event
    }
}

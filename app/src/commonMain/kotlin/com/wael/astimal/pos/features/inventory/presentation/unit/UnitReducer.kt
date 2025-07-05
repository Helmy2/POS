package com.wael.astimal.pos.features.inventory.presentation.unit

import com.wael.astimal.pos.core.base.mvi.Reducer

class UnitReducer : Reducer<UnitContract.State, UnitContract.Event, Nothing> {
    override fun reduce(
        previousState: UnitContract.State,
        event: UnitContract.Event
    ): Pair<UnitContract.State, Nothing?> {
        return when (event) {
            is UnitContract.Event.LoadingStarted ->
                previousState.copy(isLoading = true) to null

            is UnitContract.Event.LoadingFinished ->
                previousState.copy(isLoading = false) to null

            is UnitContract.Event.SearchQueryChanged ->
                previousState.copy(searchQuery = event.query) to null

            is UnitContract.Event.SearchActiveChanged ->
                previousState.copy(isSearchActive = event.isActive) to null

            is UnitContract.Event.UserLoaded ->
                previousState.copy(currentUser = event.user) to null

            is UnitContract.Event.UnitsLoaded ->
                previousState.copy(isLoading = false, units = event.units) to null

            is UnitContract.Event.UnitSelected ->
                previousState.copy(
                    selectedUnit = event.unit,
                    inputArName = event.unit.name.arName ?: "",
                    inputEnName = event.unit.name.enName ?: "",
                    inputArAbbreviation = event.unit.abbreviation.arName ?: "",
                    inputEnAbbreviation = event.unit.abbreviation.enName ?: "",
                    isSearchActive = false
                ) to null

            is UnitContract.Event.NewUnitClicked,
            is UnitContract.Event.SaveSucceeded,
            is UnitContract.Event.DeleteSucceeded ->
                // Clear the form
                previousState.copy(
                    isLoading = false,
                    selectedUnit = null,
                    inputArName = "",
                    inputEnName = "",
                ) to null

            // Form input updates
            is UnitContract.Event.ArNameChanged ->
                previousState.copy(inputArName = event.name) to null

            is UnitContract.Event.EnNameChanged ->
                previousState.copy(inputEnName = event.name) to null

            is UnitContract.Event.ArAbbreviationChanged ->
                previousState.copy(inputArAbbreviation = event.name) to null

            is UnitContract.Event.EnAbbreviationChanged ->
                previousState.copy(inputEnAbbreviation = event.name) to null

            // Events that trigger sagas in ViewModel but don't change state directly
            is UnitContract.Event.BackClicked,
            is UnitContract.Event.SaveClicked,
            is UnitContract.Event.DeleteClicked -> previousState to null
        }
    }
}

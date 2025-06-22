package com.wael.astimal.pos.features.inventory.presentation.store

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.inventory.data.entity.StoreType

class StoreReducer : Reducer<StoreContract.State, StoreContract.Event, Nothing> {
    override fun reduce(
        previousState: StoreContract.State, event: StoreContract.Event
    ): Pair<StoreContract.State, Nothing?> {
        return when (event) {
            is StoreContract.Event.LoadingStarted -> previousState.copy(isLoading = true) to null

            is StoreContract.Event.LoadingFinished -> previousState.copy(isLoading = false) to null

            is StoreContract.Event.SearchQueryChanged -> previousState.copy(searchQuery = event.query) to null

            is StoreContract.Event.SearchActiveChanged -> previousState.copy(isSearchActive = event.isActive) to null

            is StoreContract.Event.UserLoaded -> previousState.copy(currentUser = event.user) to null

            is StoreContract.Event.StoresLoaded -> previousState.copy(
                isLoading = false, stores = event.stores
            ) to null

            is StoreContract.Event.StoreSelected -> previousState.copy(
                selectedStore = event.store,
                inputArName = event.store.name.arName ?: "",
                inputEnName = event.store.name.enName ?: "",
                inputType = event.store.type,
                isSearchActive = false
            ) to null

            is StoreContract.Event.NewStoreClicked, is StoreContract.Event.SaveSucceeded, is StoreContract.Event.DeleteSucceeded -> previousState.copy(
                isLoading = false,
                selectedStore = null,
                inputArName = "",
                inputEnName = "",
                inputType = StoreType.SUB
            ) to null

            is StoreContract.Event.ArNameChanged -> previousState.copy(inputArName = event.name) to null

            is StoreContract.Event.EnNameChanged -> previousState.copy(inputEnName = event.name) to null

            is StoreContract.Event.TypeChanged -> previousState.copy(inputType = event.type) to null

            is StoreContract.Event.BackClicked, is StoreContract.Event.SaveClicked, is StoreContract.Event.DeleteClicked -> previousState to null
        }
    }
}

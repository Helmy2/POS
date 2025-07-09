package com.wael.astimal.pos.features.inventory.presentation.store

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.inventory.data.local.entity.StoreType
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.user.domain.entity.User

object StoreContract {

    data class State(
        val isLoading: Boolean = false,
        val stores: List<Store> = emptyList(),
        val selectedStore: Store? = null,
        val searchQuery: String = "",
        val isSearchActive: Boolean = false,
        val currentUser: User? = null,
        val inputArName: String = "",
        val inputEnName: String = "",
        val inputAddress: String = "",
        val inputType: StoreType = StoreType.SUB,
        val showDeleteDialog: Boolean = false
    ) : Reducer.ViewState {
        val isEditing: Boolean get() = selectedStore != null
        val canUserEdit: Boolean get() = currentUser?.isAdmin == true
        val canSave: Boolean
            get() = inputArName.isNotBlank() && inputEnName.isNotBlank()
    }

    sealed interface Event : Reducer.ViewEvent {
        data class SearchQueryChanged(val query: String) : Event
        data class SearchActiveChanged(val isActive: Boolean) : Event
        data class StoreSelected(val store: Store) : Event
        data object NewStoreClicked : Event
        data object SaveClicked : Event
        data object DeleteClicked : Event
        data object BackClicked : Event
        data object DeleteConfirmed : Event
        data object DeleteCanceled : Event

        data class ArNameChanged(val name: String) : Event
        data class EnNameChanged(val name: String) : Event
        data class AddressChanged(val address: String) : Event
        data class TypeChanged(val type: StoreType) : Event

        data class UserLoaded(val user: User?) : Event
        data class StoresLoaded(val stores: List<Store>) : Event
        data object SaveSucceeded : Event
        data object DeleteSucceeded : Event
        data object LoadingStarted : Event
        data object LoadingFinished : Event
    }
}
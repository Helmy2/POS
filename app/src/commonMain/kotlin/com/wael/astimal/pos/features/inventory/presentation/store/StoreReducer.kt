package com.wael.astimal.pos.features.inventory.presentation.store

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.util.SHOULD_SHOW_SHEATH_ON_START
import com.wael.astimal.pos.features.inventory.data.local.entity.StoreType
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.user.domain.entity.User

class StoreReducer : Reducer<StoreReducer.State, StoreReducer.Event, Nothing> {
    data class State(
        val isLoading: Boolean = false,
        val stores: List<Store> = emptyList(),
        val selectedStore: Store? = null,
        val searchQuery: String = "",
        val isSearchActive: Boolean = SHOULD_SHOW_SHEATH_ON_START,
        val currentUser: User? = null,
        val inputArName: String = "",
        val inputEnName: String = "",
        val inputAddress: String = "",
        val inputType: StoreType? = null,
        val showDeleteDialog: Boolean = false,
        val selectedEmployee: User? = null,
        val employees: List<User> = emptyList(),
    ) : Reducer.ViewState {
        val isEditing: Boolean get() = selectedStore != null
        val canUserEdit: Boolean get() = currentUser?.isAdmin == true
        val canSave: Boolean
            get() = inputArName.isNotBlank() && selectedEmployee != null
    }

    sealed interface Event : Reducer.ViewEvent {
        data class SearchQueryChanged(val query: String) : Event
        data class SearchActiveChanged(val isActive: Boolean) : Event
        data class StoreSelected(val store: Store) : Event
        data object NewStoreClicked : Event
        data object SaveClicked : Event
        data object DeleteClicked : Event
        data object DeleteConfirmed : Event
        data object DeleteCanceled : Event

        data class ArNameChanged(val name: String) : Event
        data class EnNameChanged(val name: String) : Event
        data class AddressChanged(val address: String) : Event
        data class TypeChanged(val type: StoreType?) : Event
        data class EmployeeSelected(val employee: User?) : Event

        data class UserLoaded(val currentUser: User?, val employees: List<User>) : Event
        data class StoresLoaded(val stores: List<Store>) : Event
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

            is Event.UserLoaded -> previousState.copy(
                currentUser = event.currentUser,
                employees = event.employees,
            ) to null

            is Event.EmployeeSelected -> previousState.copy(
                selectedEmployee = event.employee,
            ) to null

            is Event.StoresLoaded -> previousState.copy(
                isLoading = false, stores = event.stores
            ) to null

            is Event.StoreSelected -> previousState.copy(
                selectedStore = event.store,
                inputArName = event.store.name.arName ?: "",
                inputEnName = event.store.name.enName ?: "",
                inputType = event.store.type,
                inputAddress = event.store.address,
                selectedEmployee = event.store.employee,
                isSearchActive = false
            ) to null

            is Event.NewStoreClicked, is Event.SaveSucceeded, is Event.DeleteSucceeded -> previousState.copy(
                isLoading = false,
                selectedStore = null,
                inputArName = "",
                inputEnName = "",
                inputType = null,
                selectedEmployee = null
            ) to null

            is Event.ArNameChanged -> previousState.copy(inputArName = event.name) to null

            is Event.EnNameChanged -> previousState.copy(inputEnName = event.name) to null

            is Event.AddressChanged -> previousState.copy(inputAddress = event.address) to null

            is Event.TypeChanged -> previousState.copy(inputType = event.type) to null

            is Event.DeleteClicked -> previousState.copy(
                showDeleteDialog = true,
            ) to null

            is Event.DeleteConfirmed, is Event.DeleteCanceled -> previousState.copy(
                showDeleteDialog = false,
            ) to null

            is Event.SaveClicked -> previousState to null
        }
    }
}

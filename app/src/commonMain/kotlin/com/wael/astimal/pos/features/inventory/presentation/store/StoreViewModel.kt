package com.wael.astimal.pos.features.inventory.presentation.store

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pos.app.generated.resources.Res
import pos.app.generated.resources.error_some_field_are_required
import pos.app.generated.resources.failed_to_delete_store
import pos.app.generated.resources.failed_to_load_stores
import pos.app.generated.resources.failed_to_save_store
import pos.app.generated.resources.store_deleted_successfully
import pos.app.generated.resources.store_saved_successfully

class StoreViewModel(
    private val storeRepository: StoreRepository,
    private val userRepository: UserRepository,
    private val snackbarController: SnackbarController,
) : BaseViewModel<StoreReducer.State, StoreReducer.Event, Nothing>(
    reducer = StoreReducer(),
    initialState = StoreReducer.State()
) {
    private var searchJob: Job? = null

    init {
        loadUsers()
        searchStores("")
    }

    override fun handleEvent(event: StoreReducer.Event) {
        when (event) {
            is StoreReducer.Event.SearchQueryChanged -> {
                setState(event)
                searchStores(event.query)
            }

            is StoreReducer.Event.SaveClicked -> saveStore()
            is StoreReducer.Event.DeleteConfirmed -> deleteStore()
            else -> setState(event)
        }
    }


    private fun loadUsers() {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            val employee = userRepository.getEmployeesFlow().first()
            setState(StoreReducer.Event.UserLoaded(currentUser, employee))
        }
    }

    @OptIn(FlowPreview::class)
    private fun searchStores(query: String) {
        searchJob?.cancel()
        setState(StoreReducer.Event.LoadingStarted)
        searchJob = storeRepository.getStores(query)
            .catch {
                setState(StoreReducer.Event.LoadingFinished)
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_load_stores)))
            }
            .debounce(300L)
            .onEach { stores ->
                setState(StoreReducer.Event.StoresLoaded(stores))
            }
            .launchIn(viewModelScope)
    }

    private fun saveStore() {
        viewModelScope.launch {
            val currentState = state.value
            if (currentState.inputArName.isBlank() && currentState.inputEnName.isBlank() && currentState.inputType == null) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_some_field_are_required)))
                return@launch
            }
            setState(StoreReducer.Event.LoadingStarted)
            viewModelScope.launch {
                val storeToSave = Store(
                    id = currentState.selectedStore?.id ?: "",
                    name = LocalizedString(
                        arName = currentState.inputArName,
                        enName = currentState.inputEnName
                    ),
                    type = currentState.inputType!!,
                    createdAt = currentState.selectedStore?.createdAt ?: Clock.now(),
                    address = currentState.inputAddress,
                    employee = currentState.selectedEmployee!!
                )

                val result = storeRepository.saveStore(storeToSave)

                result.onSuccess {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.store_saved_successfully)))
                    setState(StoreReducer.Event.SaveSucceeded)
                    searchStores("")
                }.onFailure {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_save_store)))
                    setState(StoreReducer.Event.LoadingFinished)
                }
            }
        }
    }

    private fun deleteStore() {
        setState(StoreReducer.Event.DeleteConfirmed)
        val storeToDelete = state.value.selectedStore ?: return
        setState(StoreReducer.Event.LoadingStarted)
        viewModelScope.launch {
            storeRepository.deleteStore(storeToDelete).onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.store_deleted_successfully)))
                setState(StoreReducer.Event.DeleteSucceeded)
                searchStores("")
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_delete_store)))
                setState(StoreReducer.Event.LoadingFinished)
            }
        }
    }
}
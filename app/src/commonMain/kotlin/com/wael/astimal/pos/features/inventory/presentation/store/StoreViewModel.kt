package com.wael.astimal.pos.features.inventory.presentation.store

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
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
    private val navigationController: NavigationController
) : BaseViewModel<StoreContract.State, StoreContract.Event, Nothing>(
    reducer = StoreReducer(),
    initialState = StoreContract.State()
) {
    private var searchJob: Job? = null

    init {
        loadUsers()
        searchStores("")
    }

    override fun handleEvent(event: StoreContract.Event) {
        when (event) {
            is StoreContract.Event.SearchQueryChanged -> {
                setState(event)
                searchStores(event.query)
            }

            is StoreContract.Event.SaveClicked -> saveStore()
            is StoreContract.Event.DeleteConfirmed -> deleteStore()
            is StoreContract.Event.BackClicked -> navigateBack()
            else -> setState(event)
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            navigationController.navigateBack()
            setState(StoreContract.Event.LoadingFinished)
        }
    }

    private fun loadUsers() {
        viewModelScope.launch {
            val currentUser = userRepository.getCurrentUser()
            val employee = userRepository.getEmployeesFlow().first()
            setState(StoreContract.Event.UserLoaded(currentUser, employee))
        }
    }

    @OptIn(FlowPreview::class)
    private fun searchStores(query: String) {
        searchJob?.cancel()
        setState(StoreContract.Event.LoadingStarted)
        searchJob = storeRepository.getStores(query)
            .catch {
                setState(StoreContract.Event.LoadingFinished)
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_load_stores)))
            }
            .debounce(300L)
            .onEach { stores ->
                setState(StoreContract.Event.StoresLoaded(stores))
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
            setState(StoreContract.Event.LoadingStarted)
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
                    setState(StoreContract.Event.SaveSucceeded)
                    searchStores("")
                }.onFailure {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_save_store)))
                    setState(StoreContract.Event.LoadingFinished)
                }
            }
        }
    }

    private fun deleteStore() {
        setState(StoreContract.Event.DeleteConfirmed)
        val storeToDelete = state.value.selectedStore ?: return
        setState(StoreContract.Event.LoadingStarted)
        viewModelScope.launch {
            storeRepository.deleteStore(storeToDelete).onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.store_deleted_successfully)))
                setState(StoreContract.Event.DeleteSucceeded)
                searchStores("")
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_delete_store)))
                setState(StoreContract.Event.LoadingFinished)
            }
        }
    }
}
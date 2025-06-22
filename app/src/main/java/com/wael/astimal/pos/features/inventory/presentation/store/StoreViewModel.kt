package com.wael.astimal.pos.features.inventory.presentation.store

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.features.inventory.data.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

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
        loadCurrentUser()
        searchStores("")
    }

    override fun handleEvent(event: StoreContract.Event) {
        when (event) {
            is StoreContract.Event.SearchQueryChanged -> {
                setState(event)
                searchStores(event.query)
            }

            is StoreContract.Event.SaveClicked -> saveStore()
            is StoreContract.Event.DeleteClicked -> deleteStore()
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

    private fun loadCurrentUser() {
        viewModelScope.launch {
            setState(StoreContract.Event.UserLoaded(userRepository.getCurrentUser()))
        }
    }

    @OptIn(FlowPreview::class)
    private fun searchStores(query: String) {
        searchJob?.cancel()
        setState(StoreContract.Event.LoadingStarted)
        searchJob = storeRepository.getStores(query)
            .debounce(300L)
            .onEach { stores ->
                setState(StoreContract.Event.StoresLoaded(stores))
            }
            .launchIn(viewModelScope)
    }

    private fun saveStore() {
        viewModelScope.launch {
            val currentState = state.value
            if (currentState.inputArName.isBlank() && currentState.inputEnName.isBlank()) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.error_some_field_are_required)))
                return@launch
            }
            setState(StoreContract.Event.LoadingStarted)
            viewModelScope.launch {
                val storeToSave = StoreEntity(
                    localId = currentState.selectedStore?.id?.local ?: 0L,
                    serverId = currentState.selectedStore?.id?.server,
                    arName = currentState.inputArName,
                    enName = currentState.inputEnName,
                    type = currentState.inputType,
                    createdAt = currentState.selectedStore?.createdAt ?: System.currentTimeMillis()
                )

                val result = storeRepository.saveStore(storeToSave)

                result.onSuccess {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.store_saved_successfully)))
                    setState(StoreContract.Event.SaveSucceeded)
                    searchStores("")
                }.onFailure {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.failed_to_save_store)))
                    setState(StoreContract.Event.LoadingFinished)
                }
            }
        }
    }

    private fun deleteStore() {
        val storeToDelete = state.value.selectedStore ?: return
        setState(StoreContract.Event.LoadingStarted)
        viewModelScope.launch {
            storeRepository.deleteStore(storeToDelete).onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.store_deleted_successfully)))
                setState(StoreContract.Event.DeleteSucceeded)
                searchStores("")
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.failed_to_delete_store)))
                setState(StoreContract.Event.LoadingFinished)
            }
        }
    }
}
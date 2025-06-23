package com.wael.astimal.pos.features.inventory.presentation.unit

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import com.wael.astimal.pos.features.inventory.domain.repository.UnitRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class UnitViewModel(
    private val unitRepository: UnitRepository,
    private val userRepository: UserRepository,
    private val snackbarController: SnackbarController,
    private val navigationController: NavigationController
) : BaseViewModel<UnitContract.State, UnitContract.Event, Nothing>(
    reducer = UnitReducer(),
    initialState = UnitContract.State()
) {
    private var searchJob: Job? = null

    init {
        loadCurrentUser()
        searchUnits("")
    }

    override fun handleEvent(event: UnitContract.Event) {
        when (event) {
            is UnitContract.Event.SearchQueryChanged -> {
                setState(event)
                searchUnits(event.query)
            }

            is UnitContract.Event.SaveClicked -> saveUnit()
            is UnitContract.Event.DeleteClicked -> deleteUnit()
            is UnitContract.Event.BackClicked -> navigateBack()
            else -> setState(event)
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            setState(UnitContract.Event.UserLoaded(userRepository.getCurrentUser()))
        }
    }

    @OptIn(FlowPreview::class)
    private fun searchUnits(query: String) {
        searchJob?.cancel()
        setState(UnitContract.Event.LoadingStarted)
        searchJob = unitRepository.getUnits(query)
            .debounce(300L)
            .onEach { result ->
                setState(UnitContract.Event.UnitsLoaded(result.getOrDefault(emptyList())))
            }
            .launchIn(viewModelScope)
    }

    private fun saveUnit() {
        viewModelScope.launch {
            val currentState = state.value
            if (!currentState.canSave) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.error_some_field_are_required)))
                return@launch
            }
            setState(UnitContract.Event.LoadingStarted)
            viewModelScope.launch {
                val unitToSave =
                    ProductUnit(
                        id = currentState.selectedUnit?.id ?: Id.new,
                        name = LocalizedString(
                            arName = currentState.inputArName,
                            enName = currentState.inputEnName
                        ),
                        createdAt = currentState.selectedUnit?.createdAt
                            ?: Clock.now(),
                        updatedAt = Clock.now(),
                        isSynced = false
                    )

                val result =
                    unitRepository.saveUnit(unitToSave)


                result.onSuccess {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.unit_saved_successfully)))
                    setState(UnitContract.Event.SaveSucceeded)
                    searchUnits("") // Refresh the list
                }.onFailure {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.failed_to_save_unit)))
                    setState(UnitContract.Event.LoadingFinished)
                }
            }
        }
    }

    private fun deleteUnit() {
        val unitToDelete = state.value.selectedUnit ?: return
        setState(UnitContract.Event.LoadingStarted)
        viewModelScope.launch {
            unitRepository.deleteUnit(unitToDelete).onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.unit_deleted_successfully)))
                setState(UnitContract.Event.DeleteSucceeded)
                searchUnits("") // Refresh the list
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.failed_to_delete_unit)))
                setState(UnitContract.Event.LoadingFinished)
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            navigationController.navigateBack()
        }
    }
}

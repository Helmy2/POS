package com.wael.astimal.pos.features.inventory.presentation.unit

import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pos.app.generated.resources.Res
import pos.app.generated.resources.error_loading_units
import pos.app.generated.resources.error_some_field_are_required
import pos.app.generated.resources.failed_to_delete_unit
import pos.app.generated.resources.failed_to_save_unit
import pos.app.generated.resources.unit_deleted_successfully
import pos.app.generated.resources.unit_saved_successfully

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
            is UnitContract.Event.DeleteConfirmed -> deleteUnit()
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
            .catch {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_loading_units)))
                setState(UnitContract.Event.UnitsLoaded(emptyList()))
            }
            .debounce(300L)
            .onEach { result ->
                setState(UnitContract.Event.UnitsLoaded(result))
            }
            .launchIn(viewModelScope)
    }

    private fun saveUnit() {
        viewModelScope.launch {
            val currentState = state.value
            if (!currentState.canSave) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_some_field_are_required)))
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
                        isSynced = false,
                        abbreviation = LocalizedString(
                            arName = currentState.inputArAbbreviation,
                            enName = currentState.inputEnAbbreviation
                        )
                    )

                val result =
                    unitRepository.saveUnit(unitToSave)


                result.onSuccess {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.unit_saved_successfully)))
                    setState(UnitContract.Event.SaveSucceeded)
                    searchUnits("") // Refresh the list
                }.onFailure {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_save_unit)))
                    setState(UnitContract.Event.LoadingFinished)
                }
            }
        }
    }

    private fun deleteUnit() {
        setState(UnitContract.Event.DeleteConfirmed)
        val unitToDelete = state.value.selectedUnit ?: return
        setState(UnitContract.Event.LoadingStarted)
        viewModelScope.launch {
            unitRepository.deleteUnit(unitToDelete).onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.unit_deleted_successfully)))
                setState(UnitContract.Event.DeleteSucceeded)
                searchUnits("") // Refresh the list
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_delete_unit)))
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

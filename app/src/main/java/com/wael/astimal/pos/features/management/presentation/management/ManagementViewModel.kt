package com.wael.astimal.pos.features.management.presentation.management

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import kotlinx.coroutines.launch

class ManagementViewModel(
    private val navigationController: NavigationController
) : BaseViewModel<ManagementContract.State, ManagementContract.Event, Nothing>(
    reducer = ManagementReducer(),
    initialState = ManagementContract.State()
) {
    init {
        processEvent(ManagementContract.Event.LoadManagementItems)
    }

    override fun handleEvent(event: ManagementContract.Event) {
        when (event) {
            is ManagementContract.Event.ItemClicked -> {
                viewModelScope.launch {
                    navigationController.navigate(event.destination)
                }
            }

            else -> setState(event)
        }
    }
}

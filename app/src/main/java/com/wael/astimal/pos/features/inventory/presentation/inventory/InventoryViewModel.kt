package com.wael.astimal.pos.features.inventory.presentation.inventory

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val navigationController: NavigationController
) : BaseViewModel<InventoryContract.State, InventoryContract.Event, Nothing>(
    reducer = InventoryReducer(),
    initialState = InventoryContract.State()
) {
    init {
        processEvent(InventoryContract.Event.LoadInventoryItems)
    }

    override fun handleEvent(event: InventoryContract.Event) {
        viewModelScope.launch {
            when (event) {
                is InventoryContract.Event.ItemClicked -> {
                    navigationController.navigate(event.destination)
                }

                else -> setState(event)
            }
        }
    }
}

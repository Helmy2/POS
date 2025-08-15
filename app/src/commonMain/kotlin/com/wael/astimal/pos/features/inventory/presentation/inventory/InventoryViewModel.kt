package com.wael.astimal.pos.features.inventory.presentation.inventory

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val navigationController: NavigationController
) : BaseViewModel<InventoryReducer.State, InventoryReducer.Event, Nothing>(
    reducer = InventoryReducer(),
    initialState = InventoryReducer.State()
) {
    init {
        processEvent(InventoryReducer.Event.LoadInventoryItems)
    }

    override fun handleEvent(event: InventoryReducer.Event) {
        viewModelScope.launch {
            when (event) {
                is InventoryReducer.Event.ItemClicked -> {
                    navigationController.navigate(event.destination)
                }

                else -> setState(event)
            }
        }
    }
}

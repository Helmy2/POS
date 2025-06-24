package com.wael.astimal.pos.features.management.presentation.management

import androidx.annotation.StringRes
import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.domain.navigation.Destination

object ManagementContract {

    data class ManagementItem(val destination: Destination, @StringRes val label: Int)

    data class State(
        val items: List<ManagementItem> = emptyList()
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data object LoadManagementItems : Event
        data class ItemClicked(val destination: Destination) : Event
    }
}

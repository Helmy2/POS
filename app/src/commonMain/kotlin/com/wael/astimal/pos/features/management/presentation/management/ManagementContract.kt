package com.wael.astimal.pos.features.management.presentation.management

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.domain.navigation.Destination
import org.jetbrains.compose.resources.StringResource

object ManagementContract {

    data class ManagementItem(val destination: Destination, val label: StringResource)

    data class State(
        val items: List<ManagementItem> = emptyList()
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data object LoadManagementItems : Event
        data class ItemClicked(val destination: Destination) : Event
    }
}

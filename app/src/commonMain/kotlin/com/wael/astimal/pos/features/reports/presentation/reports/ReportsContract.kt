package com.wael.astimal.pos.features.reports.presentation.reports

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.domain.navigation.Destination
import org.jetbrains.compose.resources.StringResource


object ReportsContract {

    data class ReportsItem(val destination: Destination, val label: StringResource)

    data class State(
        val items: List<ReportsItem> = emptyList()
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data object LoadReports : Event
        data class ReportClicked(val destination: Destination) : Event
    }
}
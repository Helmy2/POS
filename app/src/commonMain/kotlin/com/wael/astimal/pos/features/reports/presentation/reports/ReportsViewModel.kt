package com.wael.astimal.pos.features.reports.presentation.reports

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import kotlinx.coroutines.launch

class ReportsViewModel(
    private val navigationController: NavigationController
) : BaseViewModel<ReportsReducer.State, ReportsReducer.Event, Nothing>(
    reducer = ReportsReducer(),
    initialState = ReportsReducer.State()
) {

    init {
        processEvent(ReportsReducer.Event.LoadReports)
    }

    override fun handleEvent(event: ReportsReducer.Event) {
        viewModelScope.launch {
            when (event) {
                is ReportsReducer.Event.ReportClicked -> {
                    navigationController.navigate(event.destination)
                }

                else -> setState(event)
            }
        }
    }
}

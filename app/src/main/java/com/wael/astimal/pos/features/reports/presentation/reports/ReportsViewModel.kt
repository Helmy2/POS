package com.wael.astimal.pos.features.reports.presentation.reports

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import kotlinx.coroutines.launch

class ReportsViewModel(
    private val navigationController: NavigationController
) : BaseViewModel<ReportsContract.State, ReportsContract.Event, Nothing>(
    reducer = ReportsReducer(),
    initialState = ReportsContract.State()
) {

    init {
        processEvent(ReportsContract.Event.LoadReports)
    }

    override fun handleEvent(event: ReportsContract.Event) {
        viewModelScope.launch {
            when (event) {
                is ReportsContract.Event.ReportClicked -> {
                    navigationController.navigate(event.destination)
                }

                else -> setState(event)
            }
        }
    }
}

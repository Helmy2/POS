package com.wael.astimal.pos.features.dashboard.presentation

import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.dashboard.domain.entity.DailySale

enum class TimePeriod {
    TODAY, WEEKLY, MONTHLY;

    fun getStringRes(): Int {
        return when (this) {
            TODAY -> R.string.today
            WEEKLY -> R.string.weekly
            MONTHLY -> R.string.monthly
        }
    }
}

object DashboardContract {
    data class KpiData(
        val totalRevenue: Double = 0.0,
        val totalSalesCount: Int = 0,
    )

    data class State(
        val isLoading: Boolean = true,
        val kpiData: KpiData = KpiData(),
        val salesAnalytics: List<DailySale> = emptyList(),
        val selectedTimePeriod: TimePeriod = TimePeriod.WEEKLY,
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data class TimePeriodSelected(val period: TimePeriod) : Event
        data object RefreshDataClicked : Event
        data object LoadingData : Event
        data class DataLoaded(val sales: List<DailySale>) : Event
    }
}

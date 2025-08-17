package com.wael.astimal.pos.features.dashboard.presentation

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.dashboard.domain.entity.DailySale
import com.wael.astimal.pos.features.dashboard.domain.entity.TimePeriod

class DashboardReducer : Reducer<DashboardReducer.State, DashboardReducer.Event, Nothing> {
    data class KpiData(
        val totalRevenue: Double = 0.0,
        val totalSalesCount: Int = 0,
    )

    data class State(
        val isLoading: Boolean = true,
        val kpiData: KpiData = KpiData(),
        val salesAnalytics: List<DailySale> = emptyList(),
        val selectedTimePeriod: TimePeriod = TimePeriod.TODAY,
        val havePendingTransfer: Boolean = false,
        val isLoadingSync: Boolean = false
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data class TimePeriodSelected(val period: TimePeriod) : Event
        data object RefreshDataClicked : Event
        data object LoadingData : Event
        data class LoadingSyncChange(val isLoading: Boolean) : Event
        data class DataLoaded(val sales: List<DailySale>) : Event
        data class HavePendingTransferChanged(val havePendingTransfer: Boolean) : Event
    }
    
    override fun reduce(
        previousState: State,
        event: Event
    ): Pair<State, Nothing?> {
        return when (event) {
            is Event.TimePeriodSelected ->
                previousState.copy(selectedTimePeriod = event.period) to null

            is Event.LoadingData ->
                previousState.copy(isLoading = true) to null

            is Event.DataLoaded ->
                previousState.copy(
                    isLoading = false,
                    salesAnalytics = event.sales,
                    kpiData = calculateKpi(event.sales)
                ) to null

            is Event.HavePendingTransferChanged ->
                previousState.copy(havePendingTransfer = event.havePendingTransfer) to null

            is Event.LoadingSyncChange ->
                previousState.copy(isLoadingSync = event.isLoading) to null

            is Event.RefreshDataClicked -> previousState to null
        }
    }

    private fun calculateKpi(dailySales: List<DailySale>): KpiData {
        return KpiData(
            totalRevenue = dailySales.sumOf { it.totalRevenue },
            totalSalesCount = dailySales.sumOf { it.numberOfSales }
        )
    }
}

package com.wael.astimal.pos.features.dashboard.presentation

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.dashboard.domain.entity.DailySale

class DashboardReducer : Reducer<DashboardContract.State, DashboardContract.Event, Nothing> {
    override fun reduce(
        previousState: DashboardContract.State,
        event: DashboardContract.Event
    ): Pair<DashboardContract.State, Nothing?> {
        return when (event) {
            is DashboardContract.Event.TimePeriodSelected ->
                previousState.copy(selectedTimePeriod = event.period) to null

            is DashboardContract.Event.LoadingData ->
                previousState.copy(isLoading = true) to null

            is DashboardContract.Event.DataLoaded ->
                previousState.copy(
                    isLoading = false,
                    salesAnalytics = event.sales,
                    kpiData = calculateKpi(event.sales)
                ) to null

            is DashboardContract.Event.HavePendingTransferChanged ->
                previousState.copy(havePendingTransfer = event.havePendingTransfer) to null

            is DashboardContract.Event.RefreshDataClicked -> previousState to null
        }
    }

    private fun calculateKpi(dailySales: List<DailySale>): DashboardContract.KpiData {
        return DashboardContract.KpiData(
            totalRevenue = dailySales.sumOf { it.totalRevenue },
            totalSalesCount = dailySales.sumOf { it.numberOfSales }
        )
    }
}

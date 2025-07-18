package com.wael.astimal.pos.features.dashboard.presentation

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.features.dashboard.domain.entity.DailySale
import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.monthly
import pos.app.generated.resources.today
import pos.app.generated.resources.weekly

enum class TimePeriod {
    TODAY, WEEKLY, MONTHLY;

    fun getStringRes(): StringResource {
        return when (this) {
            TODAY -> Res.string.today
            WEEKLY -> Res.string.weekly
            MONTHLY -> Res.string.monthly
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
        val selectedTimePeriod: TimePeriod = TimePeriod.TODAY,
        val havePendingTransfer: Boolean = false,
        val isLoadingSync: Boolean = false
    ) : Reducer.ViewState

    sealed interface Event : Reducer.ViewEvent {
        data class TimePeriodSelected(val period: TimePeriod) : Event
        data object RefreshDataClicked : Event
        data object LoadingData : Event
        data object PreformSync : Event
        data class LoadingSyncChange(val isLoading: Boolean) : Event
        data class DataLoaded(val sales: List<DailySale>) : Event
        data class HavePendingTransferChanged(val havePendingTransfer: Boolean) : Event
    }
}

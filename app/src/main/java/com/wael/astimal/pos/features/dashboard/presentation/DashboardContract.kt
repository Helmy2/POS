package com.wael.astimal.pos.features.dashboard.presentation

import androidx.annotation.StringRes
import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.dashboard.domain.entity.DailySale

enum class TimePeriod {
    TODAY,
    WEEKLY,
    MONTHLY;

    fun getStringRes(): Int {
        return when (this) {
            TODAY -> R.string.today
            WEEKLY -> R.string.weekly
            MONTHLY -> R.string.monthly
        }
    }
}

data class KpiData(
    val totalRevenue: Double = 0.0,
    val todaysSales: Int = 0,
    val conversionRate: Double = 0.0,
    val todaysVisits: Int = 0
)

data class DashboardState(
    val isLoading: Boolean = false,
    val kpiData: KpiData = KpiData(),
    val salesAnalytics: List<DailySale> = emptyList(),
    val selectedTimePeriod: TimePeriod = TimePeriod.WEEKLY,
    @StringRes val error: Int? = null
)

sealed interface DashboardEvent {
    data class SelectTimePeriod(val period: TimePeriod) : DashboardEvent
    data object RefreshData : DashboardEvent
    data object ClearError : DashboardEvent
}
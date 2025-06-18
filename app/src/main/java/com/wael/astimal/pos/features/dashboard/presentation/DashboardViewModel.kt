package com.wael.astimal.pos.features.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.base.UiEvent
import com.wael.astimal.pos.features.management.domain.repository.SalesOrderRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.ZoneOffset

class DashboardViewModel(
    private val salesOrderRepository: SalesOrderRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadDashboardData()
    }

    fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.SelectTimePeriod -> {
                _state.update { it.copy(selectedTimePeriod = event.period) }
                loadDashboardData()
            }

            is DashboardEvent.RefreshData -> loadDashboardData()
        }
    }

    private fun loadDashboardData() {
        _state.update { it.copy(isLoading = true) }

        val now = LocalDate.now()
        val (startDate, endDate) = when (_state.value.selectedTimePeriod) {
            TimePeriod.TODAY -> now to now
            TimePeriod.WEEKLY -> now.minusDays(6) to now
            TimePeriod.MONTHLY -> now.minusMonths(1) to now
        }

        val startMillis = startDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val endMillis = endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

        salesOrderRepository.getDailySales(startMillis, endMillis)
            .onEach { dailySales ->
                val totalRevenue = dailySales.sumOf { it.totalRevenue }
                val totalSales = dailySales.sumOf { it.numberOfSales }
                _state.update {
                    it.copy(
                        isLoading = false,
                        salesAnalytics = dailySales,
                        kpiData = it.kpiData.copy(
                            totalRevenue = totalRevenue,
                            todaysSales = totalSales
                        )
                    )
                }
            }
            .catch {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_loading_dashboard_data))
            }
            .launchIn(viewModelScope)
    }
}

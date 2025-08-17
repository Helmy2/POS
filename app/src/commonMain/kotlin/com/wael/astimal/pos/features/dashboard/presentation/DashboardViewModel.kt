package com.wael.astimal.pos.features.dashboard.presentation

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.features.dashboard.domain.entity.TimePeriod
import com.wael.astimal.pos.features.inventory.domain.repository.StockTransferRepository
import com.wael.astimal.pos.features.management.domain.repository.InvoiceRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pos.app.generated.resources.Res
import pos.app.generated.resources.error_loading_dashboard_data
import java.time.LocalDate
import java.time.ZoneOffset

class DashboardViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val snackbarController: SnackbarController,
    private val stockTransferRepository: StockTransferRepository,
) : BaseViewModel<DashboardReducer.State, DashboardReducer.Event, Nothing>(
    reducer = DashboardReducer(),
    initialState = DashboardReducer.State()
) {

    init {
        loadDashboardData()
    }

    override fun handleEvent(event: DashboardReducer.Event) {
        when (event) {
            is DashboardReducer.Event.TimePeriodSelected -> {
                setState(event)
                loadDashboardData()
            }

            is DashboardReducer.Event.RefreshDataClicked -> {
                loadDashboardData()
            }
            // All other events are for synchronous state updates only
            else -> setState(event)
        }
    }

    private fun loadDashboardData() {
        setState(DashboardReducer.Event.LoadingData)

        viewModelScope.launch {
            stockTransferRepository.getPendingTransfersForApproval().collect { pendingTransfers ->
                setState(
                    DashboardReducer.Event.HavePendingTransferChanged(
                        pendingTransfers.isNotEmpty()
                    )
                )
            }
        }

        val now = LocalDate.now()
        val (startDate, endDate) = when (state.value.selectedTimePeriod) {
            TimePeriod.TODAY -> now to now
            TimePeriod.WEEKLY -> now.minusDays(6) to now
            TimePeriod.MONTHLY -> now.minusMonths(1) to now
        }

        val startMillis = startDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val endMillis = endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

        invoiceRepository.getDailySales(startMillis, endMillis)
            .onEach { dailySales ->
                setState(DashboardReducer.Event.DataLoaded(dailySales))
            }
            .catch {
                snackbarController.sendEvent(
                    SnackbarEvent(StringResource.FromResource(Res.string.error_loading_dashboard_data))
                )
                setState(DashboardReducer.Event.DataLoaded(emptyList()))
            }
            .launchIn(viewModelScope)
    }
}

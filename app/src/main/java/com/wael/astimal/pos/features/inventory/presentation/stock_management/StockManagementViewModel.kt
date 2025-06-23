package com.wael.astimal.pos.features.inventory.presentation.stock_management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.base.UiEvent
import com.wael.astimal.pos.features.inventory.data.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StockManagementViewModel(
    private val stockRepository: StockRepository,
    private val storeRepository: StoreRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StockManagementState())
    val state: StateFlow<StockManagementState> = _state.asStateFlow()

    private var stockJob: Job? = null

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            storeRepository.getStores().catch {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_loading_stores))
            }.collect { stores ->
                _state.update { it.copy(stores = stores.getOrDefault(emptyList())) }
            }
        }
        viewModelScope.launch {
            userRepository.getCurrentUser()?.let { user ->
                _state.update { it.copy(currentUser = user) }
            }
        }
        loadStocks()
    }

    fun onEvent(event: StockManagementEvent) {
        when (event) {
            is StockManagementEvent.SearchStock -> {
                _state.update { it.copy(query = event.query) }
                loadStocks()
            }

            is StockManagementEvent.FilterByStore -> {
                _state.update { it.copy(selectedStore = event.store) }
                loadStocks()
            }

            is StockManagementEvent.ShowAdjustmentDialog -> {
                _state.update {
                    it.copy(
                        showAdjustmentDialog = true,
                        adjustmentTarget = event.stockItem,
                        adjustmentQuantityChange = "",
                        adjustmentReason = StockAdjustmentReason.RECOUNT,
                        adjustmentNotes = ""
                    )
                }
            }

            is StockManagementEvent.DismissAdjustmentDialog -> {
                _state.update { it.copy(showAdjustmentDialog = false, adjustmentTarget = null) }
            }

            is StockManagementEvent.UpdateAdjustmentQuantity -> {
                _state.update { it.copy(adjustmentQuantityChange = event.quantity) }
            }

            is StockManagementEvent.UpdateAdjustmentReason -> {
                _state.update { it.copy(adjustmentReason = event.reason) }
            }

            is StockManagementEvent.UpdateAdjustmentNotes -> {
                _state.update { it.copy(adjustmentNotes = event.notes) }
            }

            is StockManagementEvent.SaveStockAdjustment -> {
                saveStockAdjustment()
            }
        }
    }

    private fun loadStocks() {
        stockJob?.cancel()
        stockJob = stockRepository.getStoreStocks(
            query = _state.value.query, selectedStoreId = _state.value.selectedStore?.id?.local
        ).onEach { stocks ->
                _state.update { it.copy(stocks = stocks, loading = false) }
            }.catch {
            _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_loading_stock))
            }.launchIn(viewModelScope)
    }

    private fun saveStockAdjustment() {
        viewModelScope.launch {
            val target = _state.value.adjustmentTarget
            val currentUser = _state.value.currentUser
            val quantityChange = _state.value.adjustmentQuantityChange.toDoubleOrNull()

            if (target == null || currentUser == null) {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_missing_data))

                _state.update { it.copy(showAdjustmentDialog = false) }
                return@launch
            }

            if (quantityChange == null || quantityChange == 0.0) {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.invalid_quantity))
                return@launch
            }

            val adjustment = StockAdjustmentEntity(
                localId = 0L,
                serverId = null,
                storeId = target.store.id.local,
                productId = target.product.id.local,
                userId = currentUser.id,
                reason = _state.value.adjustmentReason,
                notes = _state.value.adjustmentNotes.takeIf { it.isNotBlank() },
                quantityChange = quantityChange,
                updatedAt = System.currentTimeMillis(),
                isSynced = false
            )

            try {
                stockRepository.addStockAdjustment(adjustment)
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.stock_updated_successfully))
            } catch (_: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_updating_stock))
            }
        }
    }
}

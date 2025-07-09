package com.wael.astimal.pos.features.inventory.presentation.stock_management

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustment
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pos.app.generated.resources.Res
import pos.app.generated.resources.error_loading_products
import pos.app.generated.resources.error_loading_stock
import pos.app.generated.resources.error_loading_stores
import pos.app.generated.resources.error_missing_data
import pos.app.generated.resources.error_updating_stock
import pos.app.generated.resources.failed_to_delete_store
import pos.app.generated.resources.insufficient_stock
import pos.app.generated.resources.invalid_quantity
import pos.app.generated.resources.stock_saved_successfully
import pos.app.generated.resources.store_deleted_successfully

class StockManagementViewModel(
    private val stockRepository: StockRepository,
    private val storeRepository: StoreRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val snackbarController: SnackbarController,
    private val navigationController: NavigationController
) : BaseViewModel<StockManagementContract.State, StockManagementContract.Event, Nothing>(
    reducer = StockManagementReducer(), initialState = StockManagementContract.State()
) {
    private var stockJob: Job? = null

    init {
        handleEvent(StockManagementContract.Event.LoadInitialData)
    }

    override fun handleEvent(event: StockManagementContract.Event) {
        when (event) {
            is StockManagementContract.Event.LoadInitialData -> loadInitialData()
            is StockManagementContract.Event.SearchQueryChanged -> {
                setState(event)
                loadStocks()
            }

            is StockManagementContract.Event.SaveClicked -> saveStockAdjustment()

            is StockManagementContract.Event.NavigateBack -> {
                viewModelScope.launch {
                    navigationController.navigateBack()
                }
            }

            is StockManagementContract.Event.DeleteConfirmed -> deleteStockAdjustment()

            else -> setState(event)
        }
    }

    private fun deleteStockAdjustment() {
        setState(StockManagementContract.Event.DeleteConfirmed)
        val adjustmentToRemove = state.value.selectedAdjustment ?: return
        setState(StockManagementContract.Event.LoadingStarted)
        viewModelScope.launch {
            stockRepository.deleteStockAdjustment(adjustmentToRemove).onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.store_deleted_successfully)))
                setState(StockManagementContract.Event.DeleteSucceeded)
                loadStocks()
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_delete_store)))
                setState(StockManagementContract.Event.LoadingFinished)
            }
        }
    }

    private fun loadInitialData() {
        setState(StockManagementContract.Event.LoadInitialData)
        viewModelScope.launch {
            setState(StockManagementContract.Event.UserLoaded(userRepository.getCurrentUser()))
        }
        viewModelScope.launch {
            storeRepository.getStores("").catch {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_loading_stores)))
                setState(StockManagementContract.Event.StoresLoaded(emptyList()))
            }.collect { stores ->
                setState(StockManagementContract.Event.StoresLoaded(stores))
            }
        }
        viewModelScope.launch {
            productRepository.getProducts().catch {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_loading_products)))
                setState(StockManagementContract.Event.ProductsLoaded(emptyList()))
            }.collect { it ->
                setState(StockManagementContract.Event.ProductsLoaded(it))
            }
        }
        loadStocks()
    }

    private fun loadStocks() {
        stockJob?.cancel()
        stockJob = stockRepository.getStoreStocks(
            query = state.value.query, selectedStoreId = null
        ).catch {
            snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_loading_stock)))
            setState(StockManagementContract.Event.StocksAdjustmentLoaded(emptyList())) // Clear list on error
        }.onEach {
            setState(StockManagementContract.Event.StocksAdjustmentLoaded(it))
        }.launchIn(viewModelScope)
    }

    private fun saveStockAdjustment() {
        viewModelScope.launch {
            val adjustmentStore = state.value.adjustmentStore
            val adjustmentProduct = state.value.adjustmentProduct
            val currentUser = state.value.currentUser
            val quantityChange = state.value.adjustmentQuantityChange.toDoubleOrNull()

            if (adjustmentStore == null || adjustmentProduct == null || currentUser == null) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_missing_data)))
                return@launch
            }

            if (quantityChange == null || quantityChange == 0.0) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.invalid_quantity)))
                return@launch
            }

            val currentQuantity = stockRepository.getStockQuantityFlow(
                adjustmentStore.id.local,
                adjustmentProduct.id.local
            ).first()

            if (currentQuantity + quantityChange < 0) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.insufficient_stock)))
                return@launch
            }

            val adjustment = StockAdjustment(
                id = state.value.selectedAdjustment?.id ?: Id.new,
                store = adjustmentStore,
                product = adjustmentProduct,
                user = currentUser,
                reason = state.value.adjustmentReason,
                notes = state.value.adjustmentNotes.takeIf { it.isNotBlank() },
                quantityChange = quantityChange,
                createdAt = Clock.now(),
            )

            try {
                stockRepository.addStockAdjustment(adjustment)
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.stock_saved_successfully)))
                setState(StockManagementContract.Event.AdjustmentSucceeded)
            } catch (e: Exception) {
                snackbarController.sendEvent(
                    SnackbarEvent(
                        StringResource.FromResource(
                            Res.string.error_updating_stock, e.message ?: ""
                        )
                    )
                )
                setState(StockManagementContract.Event.AdjustmentFailed)
            }
        }
    }
}

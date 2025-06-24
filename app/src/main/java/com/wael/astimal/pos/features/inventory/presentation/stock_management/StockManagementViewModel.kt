package com.wael.astimal.pos.features.inventory.presentation.stock_management

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class StockManagementViewModel(
    private val stockRepository: StockRepository,
    private val storeRepository: StoreRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val snackbarController: SnackbarController,
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

            is StockManagementContract.Event.SaveAdjustmentClicked -> saveStockAdjustment()
            else -> setState(event)
        }
    }

    private fun loadInitialData() {
        setState(StockManagementContract.Event.LoadInitialData)
        viewModelScope.launch {
            setState(StockManagementContract.Event.UserLoaded(userRepository.getCurrentUser()))
        }
        viewModelScope.launch {
            storeRepository.getStores("").catch {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.error_loading_stores)))
                setState(StockManagementContract.Event.StoresLoaded(emptyList()))
            }.collect { stores ->
                setState(StockManagementContract.Event.StoresLoaded(stores))
            }
        }
        viewModelScope.launch {
            productRepository.getProducts().catch {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.error_loading_products)))
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
            snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.error_loading_stock)))
            setState(StockManagementContract.Event.StocksLoaded(emptyList())) // Clear list on error

        }.onEach {
            setState(StockManagementContract.Event.StocksLoaded(it))
        }.launchIn(viewModelScope)
    }

    private fun saveStockAdjustment() {
        viewModelScope.launch {
            val adjustmentStore = state.value.adjustmentStore
            val adjustmentProduct = state.value.adjustmentProduct
            val currentUser = state.value.currentUser
            val quantityChange = state.value.adjustmentQuantityChange.toDoubleOrNull()

            if (adjustmentStore == null || adjustmentProduct == null || currentUser == null) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.error_missing_data)))
                setState(StockManagementContract.Event.DismissAdjustmentDialog)
                return@launch
            }

            if (quantityChange == null || quantityChange == 0.0) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.invalid_quantity)))
                return@launch
            }

            state.value.productBundles.first { it.store == adjustmentStore }.quantities.firstOrNull { it.product == adjustmentProduct }
                ?.let { productQuantity ->
                    if (productQuantity.quantity + quantityChange < 0) {
                        snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.insufficient_stock)))
                        return@launch
                    }
                } ?: run {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.product_not_found_in_store)))
                return@launch
            }

            val adjustment = StockAdjustment(
                id = Id.new,
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
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.stock_updated_successfully)))
                setState(StockManagementContract.Event.AdjustmentSucceeded)
            } catch (e: Exception) {
                snackbarController.sendEvent(
                    SnackbarEvent(
                        StringResource.FromResource(
                            R.string.error_updating_stock, e.message ?: ""
                        )
                    )
                )
                setState(StockManagementContract.Event.DismissAdjustmentDialog)
            }
        }
    }
}

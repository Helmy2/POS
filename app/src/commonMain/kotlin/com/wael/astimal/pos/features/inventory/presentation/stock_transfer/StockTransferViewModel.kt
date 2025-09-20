package com.wael.astimal.pos.features.inventory.presentation.stock_transfer

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransferItem
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockTransferRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pos.app.generated.resources.Res
import pos.app.generated.resources.failed_to_delete_transfer
import pos.app.generated.resources.failed_to_load_products
import pos.app.generated.resources.failed_to_load_stock
import pos.app.generated.resources.failed_to_load_stores
import pos.app.generated.resources.failed_to_save_transfer
import pos.app.generated.resources.from_and_to_stores_cannot_be_the_same
import pos.app.generated.resources.from_and_to_stores_must_be_selected
import pos.app.generated.resources.not_enough_stock_for
import pos.app.generated.resources.transfer_approved_successfully
import pos.app.generated.resources.transfer_deleted_successfully
import pos.app.generated.resources.transfer_must_have_at_least_one_valid_item
import pos.app.generated.resources.transfer_rejected_successfully
import pos.app.generated.resources.transfer_saved_successfully
import pos.app.generated.resources.user_not_identified_cannot_save_transfer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class StockTransferViewModel(
    private val stockTransferRepository: StockTransferRepository,
    private val storeRepository: StoreRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val stockRepository: StockRepository,
    private val snackbarController: SnackbarController,
) : BaseViewModel<StockTransferReducer.State, StockTransferReducer.Event, Nothing>(
    reducer = StockTransferReducer(), initialState = StockTransferReducer.State(
        currentTransferInput = StockTransferReducer.EditableStockTransfer()
    )
) {
    private var searchJob: Job? = null
    private val stockObservationJobs = mutableMapOf<String, Job>()

    init {
        loadCurrentUser()
        loadDropdownData()
        searchTransfers("")
    }

    override fun handleEvent(event: StockTransferReducer.Event) {
        when (event) {
            is StockTransferReducer.Event.SearchQueryChanged -> {
                setState(event)
                searchTransfers(event.query)
            }

            is StockTransferReducer.Event.TransferSelected -> {
                setState(event)
                resubscribeAllStockObservers()
            }

            is StockTransferReducer.Event.SaveClicked -> saveTransfer()
            is StockTransferReducer.Event.DeleteClicked -> deleteTransfer()
            is StockTransferReducer.Event.FromStoreChanged -> {
                setState(event)
                resubscribeAllStockObservers()
            }

            is StockTransferReducer.Event.ItemProductChanged -> {
                setState(event)
                event.product?.let { observeStockForItem(event.editorId, it.id) }
            }

            is StockTransferReducer.Event.RemoveItem -> {
                stockObservationJobs[event.editorId]?.cancel()
                stockObservationJobs.remove(event.editorId)
                setState(event)
            }

            is StockTransferReducer.Event.ApprovedClicked -> {
                viewModelScope.launch {
                    stockTransferRepository.setTransferApprovalStatus(
                        state.value.selectedTransfer!!.id, true
                    ).onSuccess {
                        snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.transfer_approved_successfully)))
                        setState(StockTransferReducer.Event.ApprovedSucceeded)
                    }.onFailure {
                        snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_save_transfer)))
                        setState(StockTransferReducer.Event.ApprovedFauild)
                    }
                }
            }

            is StockTransferReducer.Event.RejectedClicked -> {
                viewModelScope.launch {
                    stockTransferRepository.setTransferApprovalStatus(
                        state.value.selectedTransfer!!.id, false
                    ).onSuccess {
                        snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.transfer_rejected_successfully)))
                        setState(StockTransferReducer.Event.SaveSucceeded)
                    }.onFailure {
                        snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_save_transfer)))
                        setState(StockTransferReducer.Event.LoadingFinished)
                    }
                }
            }

            else -> setState(event)
        }
    }

    private fun loadCurrentUser() = viewModelScope.launch {
        val currentUser = userRepository.getCurrentUser() ?: return@launch
        setState(StockTransferReducer.Event.UserLoaded(currentUser))
    }

    private fun loadDropdownData() = viewModelScope.launch {
        val currentUser = userRepository.getCurrentUser() ?: return@launch

        combine(storeRepository.getStores("").catch {
            snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_load_stores)))
            setState(StockTransferReducer.Event.LoadingFinished)
        }, productRepository.getProducts("").catch {
            snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_load_products)))
            setState(StockTransferReducer.Event.LoadingFinished)
        }) { stores, products ->
            StockTransferReducer.DropdownData(
                stores.filter { it.employee.id == currentUser.id },
                stores.filter { it.employee.id != currentUser.id },
                products
            )
        }.collect { dropdownData ->
            setState(StockTransferReducer.Event.DropdownDataLoaded(dropdownData))
        }
    }

    @OptIn(FlowPreview::class)
    private fun searchTransfers(query: String) {
        searchJob?.cancel()
        setState(StockTransferReducer.Event.LoadingStarted)
        searchJob = stockTransferRepository.getStockTransfersWithDetails().debounce(300L)
            .onEach { transfers ->
                val filtered = if (query.isBlank()) {
                    transfers
                } else {
                    transfers.filter {
                        it.fromStore.name.contains(query) || it.toStore.name.contains(
                            query
                        )
                    }
                }
                setState(StockTransferReducer.Event.TransfersLoaded(filtered))
            }.launchIn(viewModelScope)
    }

    private fun observeStockForItem(editorId: String, productId: String) {
        stockObservationJobs[editorId]?.cancel()
        val fromStoreId = state.value.currentTransferInput.fromStore?.id ?: return
        stockObservationJobs[editorId] =
            stockRepository.getStockQuantity(fromStoreId, productId).catch {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_load_stock)))
            }.onEach { stock ->
                setState(
                    StockTransferReducer.Event.StockForItemSelected(
                        editorId, stock
                    )
                )
            }.launchIn(viewModelScope)
    }

    private fun resubscribeAllStockObservers() {
        stockObservationJobs.values.forEach { it.cancel() }
        stockObservationJobs.clear()
        state.value.currentTransferInput.items.forEach { item ->
            item.product?.id?.let { productId ->
                observeStockForItem(item.editorId, productId)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun saveTransfer() = viewModelScope.launch {
        val currentInput = state.value.currentTransferInput
        val currentUser = state.value.currentUser

        if (currentInput.fromStore == null || currentInput.toStore == null) {
            snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.from_and_to_stores_must_be_selected)))
            return@launch
        }
        if (currentInput.fromStore.id == currentInput.toStore.id) {
            snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.from_and_to_stores_cannot_be_the_same)))
            return@launch
        }
        if (currentInput.items.isEmpty() || currentInput.items.any { it.product == null }) {
            snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.transfer_must_have_at_least_one_valid_item)))
            return@launch
        }
        if (currentUser == null) {
            snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.user_not_identified_cannot_save_transfer)))
            return@launch
        }

        // Validate stock levels
        for (item in currentInput.items) {
            if ((item.maxUnitQuantity.toDoubleOrNull() ?: 0.0) > item.currentMaxStock) {
                snackbarController.sendEvent(
                    SnackbarEvent(
                        StringResource.FromResourceAndLocalizedString(
                            Res.string.not_enough_stock_for, item.product?.name
                        ) { res, name -> "$res $name" })
                )
                return@launch
            }
        }

        setState(StockTransferReducer.Event.LoadingStarted)

        val transferItems = currentInput.items.mapNotNull {
            val quantity = it.maxUnitQuantity.toDoubleOrNull()
            if (it.product != null && quantity != null && quantity > 0) {
                StockTransferItem(
                    product = it.product, quantity = quantity, id = Uuid.random().toString()
                )
            } else null
        }

        val result = if (state.value.isEditing) {
            stockTransferRepository.updateStockTransfer(
                transferLocalId = state.value.selectedTransfer!!.id,
                fromStore = currentInput.fromStore,
                toStore = currentInput.toStore,
                initiatedByUser = currentInput.selectedEmployee!!,
                receivingUser = currentInput.toStore.employee,
                items = transferItems,
                transferDate = currentInput.transferDate,
                notes = currentInput.notes,
                status = state.value.selectedTransfer!!.status,
                createdAt = state.value.selectedTransfer!!.createdAt
            )
        } else {
            stockTransferRepository.addStockTransfer(
                fromStore = currentInput.fromStore,
                toStore = currentInput.toStore,
                initiatedByUser = currentInput.selectedEmployee!!,
                receivingUser = currentInput.toStore.employee,
                items = transferItems,
                transferDate = currentInput.transferDate,
                notes = currentInput.notes
            )
        }

        result.onSuccess {
            snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.transfer_saved_successfully)))
            setState(StockTransferReducer.Event.SaveSucceeded)
            searchTransfers("")
        }.onFailure {
            snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_save_transfer)))
            setState(StockTransferReducer.Event.LoadingFinished)
        }
    }

    private fun deleteTransfer() = viewModelScope.launch {
        val transferToDelete = state.value.selectedTransfer ?: return@launch
        setState(StockTransferReducer.Event.LoadingStarted)
        stockTransferRepository.deleteStockTransfer(transferToDelete).onSuccess {
            snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.transfer_deleted_successfully)))
            setState(StockTransferReducer.Event.DeleteSucceeded)
            searchTransfers("")
        }.onFailure {
            snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_delete_transfer)))
            setState(StockTransferReducer.Event.LoadingFinished)
        }
    }
}

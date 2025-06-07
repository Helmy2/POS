package com.wael.astimal.pos.features.inventory.presentation.stock_transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.features.inventory.data.entity.StockTransferItemEntity
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockTransferRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StockTransferViewModel(
    private val stockTransferRepository: StockTransferRepository,
    private val storeRepository: StoreRepository,
    private val productRepository: ProductRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(StockTransferScreenState())
    val state: StateFlow<StockTransferScreenState> = _state.asStateFlow()

    private var searchJob: Job? = null
    private var currentUserId: Long? = null

    init {
        viewModelScope.launch {
            sessionManager.getCurrentUser().collect { userSession ->
                currentUserId = userSession?.id
            }
        }
        viewModelScope.launch {
            onEvent(StockTransferScreenEvent.LoadTransfers)
        }
        loadDropdownData()
    }

    private fun loadDropdownData() {
        viewModelScope.launch {
            storeRepository.getStores("").collect { stores ->
                _state.update { it.copy(availableStores = stores) }
            }
        }
        viewModelScope.launch {
            productRepository.getProducts("").collect { products ->
                _state.update { it.copy(availableProducts = products) }
            }
        }
    }

    fun onEvent(event: StockTransferScreenEvent) {
        when (event) {
            is StockTransferScreenEvent.LoadTransfers -> searchTransfers(_state.value.query)
            is StockTransferScreenEvent.SearchTransfers -> searchTransfers(event.query)
            is StockTransferScreenEvent.SelectTransferToView -> {
                _state.update {
                    it.copy(
                        selectedTransfer = event.transfer, isDetailViewOpen = event.transfer != null
                    )
                }
                if (event.transfer == null) { // If deselecting, reset form
                    _state.update { it.copy(currentTransferInput = EditableStockTransfer()) }
                } else {
                    // Populate form if viewing/editing existing (status update only for existing)
                    _state.update {
                        it.copy(
                            currentTransferInput = EditableStockTransfer(
                                localId = event.transfer.localId,
                                fromStoreId = event.transfer.fromStore?.localId,
                                toStoreId = event.transfer.toStore?.localId,
                                items = event.transfer.items.map { item ->
                                    EditableStockTransferItem(
                                        tempEditorId = item.localId, // Use item's localId as editorId
                                        product = item.product,
                                        unit = item.unit,
                                        quantity = item.quantity.toString(),
                                        maxOpeningBalance = item.maximumOpeningBalance?.toString()
                                            ?: "",
                                        minOpeningBalance = item.minimumOpeningBalance?.toString()
                                            ?: ""
                                    )
                                }.toMutableList(),
                            )
                        )
                    }
                }
            }

            is StockTransferScreenEvent.OpenNewTransferForm -> {
                _state.update {
                    it.copy(
                        isDetailViewOpen = true,
                        selectedTransfer = null,
                        currentTransferInput = EditableStockTransfer()
                    )
                }
            }

            is StockTransferScreenEvent.CloseTransferForm -> {
                _state.update {
                    it.copy(
                        isDetailViewOpen = false,
                        selectedTransfer = null,
                        currentTransferInput = EditableStockTransfer()
                    )
                }
            }

            is StockTransferScreenEvent.UpdateFromStore -> _state.update { s ->
                s.copy(
                    currentTransferInput = s.currentTransferInput.copy(fromStoreId = event.storeId)
                )
            }

            is StockTransferScreenEvent.UpdateToStore -> _state.update { s ->
                s.copy(
                    currentTransferInput = s.currentTransferInput.copy(toStoreId = event.storeId)
                )
            }

            is StockTransferScreenEvent.AddItemToTransfer -> {
                val currentInput = _state.value.currentTransferInput
                val newItems =
                    currentInput.items.toMutableList().apply { add(EditableStockTransferItem()) }
                _state.update { it.copy(currentTransferInput = currentInput.copy(items = newItems)) }
            }

            is StockTransferScreenEvent.RemoveItemFromTransfer -> {
                val currentInput = _state.value.currentTransferInput
                val updatedItems =
                    currentInput.items.filterNot { it.tempEditorId == event.itemEditorId }
                        .toMutableList()
                _state.update { it.copy(currentTransferInput = currentInput.copy(items = updatedItems)) }
            }

            is StockTransferScreenEvent.UpdateItemProduct -> updateTransferItem(event.itemEditorId) {
                it.copy(product = event.product)
            }

            is StockTransferScreenEvent.UpdateItemUnit -> updateTransferItem(event.itemEditorId) {
                it.copy(unit = event.unit)
            }

            is StockTransferScreenEvent.UpdateItemQuantity -> updateTransferItem(event.itemEditorId) {
                it.copy(
                    quantity = event.quantity
                )
            }
            // Optional item balance updates
            // is StockTransferScreenEvent.UpdateItemMaxOpeningBalance -> updateTransferItem(event.itemEditorId) { it.copy(maxOpeningBalance = event.balance) }
            // is StockTransferScreenEvent.UpdateItemMinOpeningBalance -> updateTransferItem(event.itemEditorId) { it.copy(minOpeningBalance = event.balance) }
            is StockTransferScreenEvent.SaveTransfer -> saveCurrentTransfer()

            is StockTransferScreenEvent.DeleteTransfer -> deleteTransfer(event.transferLocalId)
            is StockTransferScreenEvent.ClearSnackbar -> _state.update { it.copy(snackbarMessage = null) }
            is StockTransferScreenEvent.UpdateIsQueryActive -> _state.update { it.copy(isQueryActive = event.isQueryActive) }
        }
    }

    private fun updateTransferItem(
        itemEditorId: Long, updateAction: (EditableStockTransferItem) -> EditableStockTransferItem
    ) {
        val currentInput = _state.value.currentTransferInput
        val updatedItems = currentInput.items.map {
            if (it.tempEditorId == itemEditorId) updateAction(it) else it
        }.toMutableList()
        _state.update { it.copy(currentTransferInput = currentInput.copy(items = updatedItems)) }
    }


    private fun searchTransfers(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            // For stock transfers, query might be on store names, dates, or status.
            // The repository's getStockTransfersWithDetails currently doesn't take a query.
            // We'll filter client-side for now, or update repository if server-side search is needed.
            stockTransferRepository.getStockTransfersWithDetails().catch { e ->
                _state.update {
                    it.copy(
                        loading = false, error = "Error fetching transfers: ${e.message}"
                    )
                }
            }.map { transfers ->
                if (query.isBlank()) {
                    transfers
                } else {
                    transfers.filter { transfer ->
                        (transfer.fromStore?.localizedName?.arName?.contains(
                            query, ignoreCase = true
                        ) == true) || (transfer.toStore?.localizedName?.enName?.contains(
                            query, ignoreCase = true
                        ) == true) || (transfer.initiatedByUser?.enName?.contains(
                            query, ignoreCase = true
                        ) == true) || (SimpleDateFormat(
                            "dd/MM/yyyy", Locale.getDefault()
                        ).format(Date(transfer.transferDate)).contains(query))
                    }
                }
            }.collect { filteredTransfers ->
                _state.update { it.copy(loading = false, transfers = filteredTransfers) }
            }
        }
    }

    private fun saveCurrentTransfer() {
        val currentInput = _state.value.currentTransferInput
        val loggedInUserId = currentUserId?.toLong()

        if (currentInput.fromStoreId == null || currentInput.toStoreId == null) {
            _state.update { it.copy(error = "From and To stores must be selected.") }
            return
        }
        if (currentInput.fromStoreId == currentInput.toStoreId) {
            _state.update { it.copy(error = "From and To stores cannot be the same.") }
            return
        }
        if (currentInput.items.isEmpty()) {
            _state.update { it.copy(error = "Transfer must have at least one item.") }
            return
        }
        if (loggedInUserId == null) {
            _state.update { it.copy(error = "User not identified. Cannot save transfer.") }
            return
        }

        val itemEntities = currentInput.items.mapNotNull { editableItem ->
            if (editableItem.product == null || editableItem.unit == null || editableItem.quantity.toDoubleOrNull() == null || editableItem.quantity.toDouble() <= 0) {
                _state.update { it.copy(error = "All items must have a product, unit, and valid quantity.") }
                return@mapNotNull null
            }
            StockTransferItemEntity(
                serverId = null,
                stockTransferLocalId = 0L,
                productLocalId = editableItem.product?.localId!!,
                unitLocalId = editableItem.unit?.localId!!,
                quantity = editableItem.quantity.toDoubleOrNull() ?: 0.0,
                maximumOpeningBalance = editableItem.maxOpeningBalance.toDoubleOrNull(),
                minimumOpeningBalance = editableItem.minOpeningBalance.toDoubleOrNull(),
            )
        }

        if (itemEntities.size != currentInput.items.size) return // An item was invalid

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val result = stockTransferRepository.addStockTransfer(
                fromStoreId = currentInput.fromStoreId!!,
                toStoreId = currentInput.toStoreId!!,
                initiatedByUserId = loggedInUserId,
                items = itemEntities
            )

            result.fold(onSuccess = {
                _state.update {
                    it.copy(
                        loading = false,
                        isDetailViewOpen = false,
                        currentTransferInput = EditableStockTransfer(),
                        snackbarMessage = "Transfer saved successfully."
                    )
                }
                onEvent(StockTransferScreenEvent.LoadTransfers) // Refresh list
            }, onFailure = { e ->
                _state.update {
                    it.copy(
                        loading = false, error = "Failed to save transfer: ${e.message}"
                    )
                }
            })
        }
    }

    private fun deleteTransfer(transferLocalId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val result = stockTransferRepository.deleteStockTransfer(transferLocalId)
            result.fold(onSuccess = {
                _state.update {
                    it.copy(
                        loading = false,
                        selectedTransfer = null,
                        snackbarMessage = "Transfer deleted."
                    )
                }
                onEvent(StockTransferScreenEvent.LoadTransfers) // Refresh list
            }, onFailure = { e ->
                _state.update {
                    it.copy(
                        loading = false, error = "Failed to delete transfer: ${e.message}"
                    )
                }
            })
        }
    }
}
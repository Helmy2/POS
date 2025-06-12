package com.wael.astimal.pos.features.inventory.presentation.stock_transfer

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.inventory.data.entity.StockTransferItemEntity
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockTransferRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
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
    private val userRepository: UserRepository,
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
            storeRepository.getStores().collect { stores ->
                _state.update { it.copy(availableStores = stores) }
            }
        }
        viewModelScope.launch {
            productRepository.getProducts().collect { products ->
                _state.update { it.copy(availableProducts = products) }
            }
        }
        viewModelScope.launch {
            userRepository.getEmployeesFlow().collect { employees ->
                _state.update { it.copy(availableEmployees = employees) }
            }
        }
    }

    fun onEvent(event: StockTransferScreenEvent) {
        when (event) {
            is StockTransferScreenEvent.LoadTransfers -> searchTransfers(_state.value.query)
            is StockTransferScreenEvent.SearchTransfers -> searchTransfers(event.query)
            is StockTransferScreenEvent.SelectTransferToView -> updateSelectedTransfer(event.transfer)
            is StockTransferScreenEvent.OpenNewTransferForm -> clear()

            is StockTransferScreenEvent.UpdateFromStore -> _state.update { currentState ->
                currentState.copy(
                    currentTransferInput = currentState.currentTransferInput.copy(
                        fromStoreId = event.storeId
                    )
                )
            }

            is StockTransferScreenEvent.UpdateToStore -> _state.update { currentState ->
                currentState.copy(
                    currentTransferInput = currentState.currentTransferInput.copy(
                        toStoreId = event.storeId
                    )
                )
            }

            is StockTransferScreenEvent.AddItemToTransfer -> {
                val currentInput = _state.value.currentTransferInput
                val newItems =
                    currentInput.items.toMutableList().apply { add(EditableStockTransferItem()) }
                _state.update {
                    it.copy(currentTransferInput = currentInput.copy(items = newItems))
                }
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
                it.copy(productUnit = event.productUnit)
            }

            is StockTransferScreenEvent.UpdateItemQuantity -> updateTransferItem(event.itemEditorId) {
                it.copy(quantity = event.quantity)
            }

            is StockTransferScreenEvent.SaveTransfer -> saveCurrentTransfer()

            is StockTransferScreenEvent.DeleteTransfer -> deleteTransfer(event.transferLocalId)
            is StockTransferScreenEvent.ClearSnackbar -> _state.update { it.copy(snackbarMessage = null) }
            is StockTransferScreenEvent.UpdateIsQueryActive -> _state.update { it.copy(isQueryActive = event.isQueryActive) }
            StockTransferScreenEvent.ClearError -> _state.update { it.copy(error = null) }
            is StockTransferScreenEvent.SelectEmployee -> {
                _state.update { currentState ->
                    currentState.copy(
                        currentTransferInput = currentState.currentTransferInput.copy(
                            selectedEmployeeId = event.id
                        )
                    )
                }
            }

            is StockTransferScreenEvent.UpdateTransferDate -> _state.update { currentState ->
                currentState.copy(
                    currentTransferInput = currentState.currentTransferInput.copy(
                        transferDate = event.date
                    )
                )
            }
        }
    }

    private fun updateSelectedTransfer(
        transfer: StockTransfer?,
    ) {
        _state.update {
            it.copy(
                selectedTransfer = transfer, isQueryActive = false
            )
        }
        if (transfer == null) {
            _state.update { it.copy(currentTransferInput = EditableStockTransfer()) }
        } else {
            _state.update {
                it.copy(
                    currentTransferInput = EditableStockTransfer(
                        localId = transfer.localId,
                        fromStoreId = transfer.fromStore?.localId,
                        toStoreId = transfer.toStore?.localId,
                        transferDate = transfer.transferDate,
                        items = transfer.items.map { item ->
                            EditableStockTransferItem(
                                tempEditorId = item.localId,
                                product = item.product,
                                productUnit = item.productUnit,
                                quantity = item.quantity.toString(),
                                maxOpeningBalance = item.maximumOpeningBalance?.toString() ?: "",
                                minOpeningBalance = item.minimumOpeningBalance?.toString() ?: "",
                            )
                        }.toMutableList(),
                    )
                )
            }
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

            stockTransferRepository.getStockTransfersWithDetails().catch {
                _state.update {
                    it.copy(loading = false, error = R.string.error_fetching_transfers)
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
        val loggedInUserId = currentUserId

        if (currentInput.fromStoreId == null || currentInput.toStoreId == null) {
            _state.update { it.copy(error = R.string.from_and_to_stores_must_be_selected) }
            return
        }
        if (currentInput.fromStoreId == currentInput.toStoreId) {
            _state.update { it.copy(error = R.string.from_and_to_stores_cannot_be_the_same) }
            return
        }
        if (currentInput.items.isEmpty()) {
            _state.update { it.copy(error = R.string.transfer_must_have_at_least_one_item) }
            return
        }
        if (loggedInUserId == null) {
            _state.update { it.copy(error = R.string.user_not_identified_cannot_save_transfer) }
            return
        }

        val itemEntities = currentInput.items.mapNotNull { editableItem ->
            if (editableItem.product == null || editableItem.productUnit == null || editableItem.quantity.toDoubleOrNull() == null || editableItem.quantity.toDouble() <= 0) {
                _state.update { it.copy(error = R.string.all_items_must_have_a_product_unit_and_valid_quantity) }
                return@mapNotNull null
            }
            StockTransferItemEntity(
                serverId = null,
                stockTransferLocalId = 0L,
                productLocalId = editableItem.product.localId,
                unitLocalId = editableItem.productUnit.localId,
                quantity = editableItem.quantity.toDoubleOrNull() ?: 0.0,
                maximumOpeningBalance = editableItem.maxOpeningBalance.toDoubleOrNull(),
                minimumOpeningBalance = editableItem.minOpeningBalance.toDoubleOrNull(),
            )
        }

        if (itemEntities.size != currentInput.items.size) return


        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }

            val result = if (_state.value.isNew) {
                stockTransferRepository.addStockTransfer(
                    fromStoreId = currentInput.fromStoreId,
                    toStoreId = currentInput.toStoreId,
                    transferDate = currentInput.transferDate ?: System.currentTimeMillis(),
                    initiatedByUserId = _state.value.currentTransferInput.selectedEmployeeId
                        ?: loggedInUserId,
                    items = itemEntities
                )
            } else {
                stockTransferRepository.updateStockTransfer(
                    transferLocalId = currentInput.localId,
                    fromStoreId = currentInput.fromStoreId,
                    toStoreId = currentInput.toStoreId,
                    transferDate = currentInput.transferDate ?: System.currentTimeMillis(),
                    initiatedByUserId = _state.value.currentTransferInput.selectedEmployeeId
                        ?: loggedInUserId,
                    items = itemEntities
                )
            }

            result.fold(onSuccess = {
                clear(snackbarMessage = R.string.transfer_saved_successfully)
                onEvent(StockTransferScreenEvent.LoadTransfers)
            }, onFailure = { _ ->
                _state.update {
                    it.copy(
                        loading = false, error = R.string.failed_to_save_transfer
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
                clear(snackbarMessage = R.string.transfer_deleted_successfully)
                onEvent(StockTransferScreenEvent.LoadTransfers)
            }, onFailure = {
                _state.update {
                    it.copy(
                        loading = false, error = R.string.failed_to_delete_transfer
                    )
                }
            })
        }
    }

    private fun clear(
        @StringRes error: Int? = null, @StringRes snackbarMessage: Int? = null
    ) {
        _state.update {
            it.copy(
                error = error,
                snackbarMessage = snackbarMessage,
                loading = false,
                isQueryActive = false,
                selectedTransfer = null,
                currentTransferInput = EditableStockTransfer()
            )
        }
    }
}
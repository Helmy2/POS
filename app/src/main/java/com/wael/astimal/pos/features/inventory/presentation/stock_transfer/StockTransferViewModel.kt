package com.wael.astimal.pos.features.inventory.presentation.stock_transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.domain.entity.Language
import com.wael.astimal.pos.features.inventory.data.entity.StockTransferItemEntity
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockTransferRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.user.domain.entity.User
import com.wael.astimal.pos.features.user.domain.entity.UserType
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StockTransferViewModel(
    private val stockTransferRepository: StockTransferRepository,
    private val storeRepository: StoreRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val stockRepository: StockRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(StockTransferScreenState())
    val state: StateFlow<StockTransferScreenState> = _state.asStateFlow()
    private var searchJob: Job? = null
    private val stockObservationJobs = mutableMapOf<String, Job>()

    init {
        viewModelScope.launch {
            sessionManager.getCurrentUser().collect { updateCurrentUser(it) }
        }
        onEvent(StockTransferScreenEvent.SearchTransfers(""))
        loadDropdownData()
    }

    private fun updateCurrentUser(user: User?) {
        _state.update {
            when {
                user == null -> it
                user.userType == UserType.ADMIN -> it.copy(currentUser = user)
                else -> it.copy(
                    currentUser = user,
                    currentTransferInput = it.currentTransferInput.copy(selectedEmployeeId = user.id)
                )
            }
        }
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
            is StockTransferScreenEvent.SearchTransfers -> searchTransfers(event.query)
            is StockTransferScreenEvent.SelectTransferToView -> updateSelectedTransfer(event.transfer)
            is StockTransferScreenEvent.OpenNewTransferForm -> clear()
            is StockTransferScreenEvent.UpdateFromStore -> {
                _state.update {
                    it.copy(
                        currentTransferInput = it.currentTransferInput.copy(
                            fromStoreId = event.store?.localId
                        )
                    )
                }
                resubscribeAllStockObservers()
            }

            is StockTransferScreenEvent.UpdateToStore -> _state.update {
                it.copy(
                    currentTransferInput = it.currentTransferInput.copy(toStoreId = event.store?.localId)
                )
            }

            is StockTransferScreenEvent.AddItemToTransfer -> {
                val currentInput = _state.value.currentTransferInput
                _state.update {
                    it.copy(currentTransferInput = currentInput.copy(items = currentInput.items + EditableStockTransferItem()))
                }
            }

            is StockTransferScreenEvent.RemoveItemFromTransfer -> {
                stockObservationJobs[event.itemEditorId]?.cancel()
                stockObservationJobs.remove(event.itemEditorId)
                val currentInput = _state.value.currentTransferInput
                val updatedItems =
                    currentInput.items.filterNot { it.tempEditorId == event.itemEditorId }
                _state.update { it.copy(currentTransferInput = currentInput.copy(items = updatedItems)) }
            }

            is StockTransferScreenEvent.UpdateItemProduct -> {
                updateTransferItem(event.itemEditorId) {
                    val conversionFactor = event.product?.subUnitsPerMainUnit ?: 1.0
                    it.copy(
                        product = event.product,
                        minUnitQuantity = conversionFactor.toString(),
                        maxUnitQuantity = "1.0",
                    )
                }
                event.product?.let { observeStockForItem(event.itemEditorId, it.localId) }
            }

            is StockTransferScreenEvent.UpdateItemUnit -> updateTransferItem(event.itemEditorId) {
                it.copy(isSelectedUnitIsMax = event.isMaxUnitSelected)
            }

            is StockTransferScreenEvent.UpdateItemMaxUnitQuantity -> updateTransferItem(event.itemEditorId) {
                val conversionFactor = it.product?.subUnitsPerMainUnit ?: 1.0
                it.copy(
                    maxUnitQuantity = event.quantity,
                    minUnitQuantity = (event.quantity.toDoubleOrNull()
                        ?.times(conversionFactor))?.toString() ?: "0.0"
                )
            }

            is StockTransferScreenEvent.UpdateItemMinUnitQuantity -> updateTransferItem(event.itemEditorId) {
                val conversionFactor = it.product?.subUnitsPerMainUnit ?: 1.0
                it.copy(
                    minUnitQuantity = event.quantity,
                    maxUnitQuantity = (event.quantity.toDoubleOrNull()
                        ?.div(conversionFactor))?.toString() ?: "0.0"
                )
            }

            is StockTransferScreenEvent.SaveTransfer -> saveCurrentTransfer()
            is StockTransferScreenEvent.DeleteTransfer -> deleteTransfer(event.transferLocalId)
            is StockTransferScreenEvent.ClearSnackbar -> _state.update { it.copy(snackbarMessage = null) }
            is StockTransferScreenEvent.UpdateIsQueryActive -> _state.update { it.copy(isQueryActive = event.isQueryActive) }
            StockTransferScreenEvent.ClearError -> _state.update { it.copy(error = null) }
            is StockTransferScreenEvent.SelectEmployee -> {
                _state.update {
                    it.copy(
                        currentTransferInput = it.currentTransferInput.copy(
                            selectedEmployeeId = event.id
                        )
                    )
                }
            }

            is StockTransferScreenEvent.UpdateTransferDate -> _state.update {
                it.copy(currentTransferInput = it.currentTransferInput.copy(transferDate = event.date))
            }
        }
    }

    private fun observeStockForItem(tempId: String, productId: Long) {
        stockObservationJobs[tempId]?.cancel()
        val fromStoreId = _state.value.currentTransferInput.fromStoreId ?: return
        stockObservationJobs[tempId] =
            stockRepository.getStockQuantityFlow(fromStoreId, productId).onEach { stock ->
                    updateTransferItem(tempId) { it.copy(currentStock = stock) }
                }.launchIn(viewModelScope)
    }

    private fun resubscribeAllStockObservers() {
        _state.value.currentTransferInput.items.forEach { item ->
            item.product?.let { product ->
                observeStockForItem(item.tempEditorId, product.localId)
            }
        }
    }

    private fun updateSelectedTransfer(transfer: StockTransfer?) {
        stockObservationJobs.values.forEach { it.cancel() }
        stockObservationJobs.clear()

        _state.update {
            it.copy(
                selectedTransfer = transfer,
                isQueryActive = false,
                currentTransferInput = if (transfer == null) EditableStockTransfer(
                    selectedEmployeeId = it.currentUser?.id
                ) else EditableStockTransfer(
                    localId = transfer.localId,
                    fromStoreId = transfer.fromStore?.localId,
                    toStoreId = transfer.toStore?.localId,
                    transferDate = transfer.transferDate,
                    selectedEmployeeId = transfer.initiatedByUser?.id,
                    items = transfer.items.map { item ->
                        val conversionFactor = item.product?.subUnitsPerMainUnit ?: 1.0
                        EditableStockTransferItem(
                            tempEditorId = item.localId.toString(),
                            product = item.product,
                            isSelectedUnitIsMax = item.productUnit?.localId == item.product?.maximumProductUnit?.localId,
                            maxUnitQuantity = item.quantity.toString(),
                            minUnitQuantity = (item.quantity * conversionFactor).toString(),
                        )
                    },
                )
            )
        }
        resubscribeAllStockObservers()
    }

    private fun updateTransferItem(
        itemEditorId: String, updateAction: (EditableStockTransferItem) -> EditableStockTransferItem
    ) {
        val currentInput = _state.value.currentTransferInput
        val updatedItems = currentInput.items.map {
            if (it.tempEditorId == itemEditorId) updateAction(it) else it
        }
        _state.update { it.copy(currentTransferInput = currentInput.copy(items = updatedItems)) }
    }

    private fun searchTransfers(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, query = query) }
            stockTransferRepository.getStockTransfersWithDetails().catch {
                    _state.update {
                        it.copy(
                            loading = false, error = R.string.error_fetching_transfers
                        )
                    }
                }.collect { transfers ->
                    val filtered = if (query.isBlank()) {
                        transfers
                    } else {
                        transfers.filter { transfer ->
                            val fromStoreName =
                                transfer.fromStore?.name?.displayName(Language.Arabic) ?: ""
                            val toStoreName =
                                transfer.toStore?.name?.displayName(Language.English) ?: ""
                            fromStoreName.contains(query, true) || toStoreName.contains(query, true)
                        }
                    }
                    _state.update { it.copy(loading = false, transfers = filtered) }
                }
        }
    }

    private fun saveCurrentTransfer() {
        viewModelScope.launch {
            val currentInput = _state.value.currentTransferInput
            val loggedInUserId = _state.value.currentUser?.id

            if (currentInput.fromStoreId == null || currentInput.toStoreId == null) {
                _state.update { it.copy(error = R.string.from_and_to_stores_must_be_selected) }
                return@launch
            }
            if (currentInput.fromStoreId == currentInput.toStoreId) {
                _state.update { it.copy(error = R.string.from_and_to_stores_cannot_be_the_same) }
                return@launch
            }
            if (currentInput.items.isEmpty()) {
                _state.update { it.copy(error = R.string.transfer_must_have_at_least_one_item) }
                return@launch
            }
            if (loggedInUserId == null) {
                _state.update { it.copy(error = R.string.user_not_identified_cannot_save_transfer) }
                return@launch
            }

            for (item in currentInput.items) {
                val quantity = item.maxUnitQuantity.toDoubleOrNull() ?: 0.0
                if (quantity > item.currentStock) {
                    val productName =
                        item.product?.localizedName?.displayName(Language.English) ?: "Product"
                    _state.update {
                        it.copy(
                            error = R.string.not_enough_stock,
                            snackbarMessage = productName.hashCode()
                        )
                    }
                    return@launch
                }
            }

            val itemEntities = currentInput.items.mapNotNull { editableItem ->
                val quantity = editableItem.maxUnitQuantity.toDoubleOrNull() ?: 0.0
                if (editableItem.product == null || quantity <= 0) {
                    return@mapNotNull null
                }
                StockTransferItemEntity(
                    stockTransferLocalId = 0L,
                    productLocalId = editableItem.product.localId,
                    unitLocalId = editableItem.product.maximumProductUnit.localId,
                    quantity = quantity,
                    serverId = null,
                )
            }

            if (itemEntities.size != currentInput.items.size) {
                _state.update { it.copy(error = R.string.one_or_more_order_items_are_invalid) }
                return@launch
            }

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
                onEvent(StockTransferScreenEvent.SearchTransfers(""))
            }, onFailure = { e ->
                _state.update {
                    it.copy(
                        loading = false,
                        error = R.string.failed_to_save_transfer,
                        snackbarMessage = e.message?.toIntOrNull()
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
                onEvent(StockTransferScreenEvent.SearchTransfers(""))
            }, onFailure = {
                _state.update {
                    it.copy(
                        loading = false, error = R.string.failed_to_delete_transfer
                    )
                }
            })
        }
    }

    private fun clear(snackbarMessage: Int? = null) {
        stockObservationJobs.values.forEach { it.cancel() }
        stockObservationJobs.clear()
        _state.update {
            it.copy(
                snackbarMessage = snackbarMessage,
                loading = false,
                isQueryActive = false,
                selectedTransfer = null,
                currentTransferInput = EditableStockTransfer()
            )
        }
        updateCurrentUser(_state.value.currentUser)
    }
}

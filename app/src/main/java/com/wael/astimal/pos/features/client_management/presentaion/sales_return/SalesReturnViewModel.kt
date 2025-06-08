package com.wael.astimal.pos.features.client_management.presentaion.sales_return

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.client_management.data.entity.OrderReturnEntity
import com.wael.astimal.pos.features.client_management.data.entity.OrderReturnProductEntity
import com.wael.astimal.pos.features.client_management.domain.repository.ClientRepository
import com.wael.astimal.pos.features.client_management.domain.repository.SalesReturnRepository
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SalesReturnViewModel(
    private val salesReturnRepository: SalesReturnRepository,
    private val clientRepository: ClientRepository,
    private val productRepository: ProductRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(SalesReturnScreenState())
    val state: StateFlow<SalesReturnScreenState> = _state.asStateFlow()
    private var searchJob: Job? = null
    private var currentUserId: Long? = null

    init {
        viewModelScope.launch {
            sessionManager.getCurrentUser().collect { user ->
                currentUserId = user?.id?.toLong()
                if (_state.value.newReturnInput.selectedEmployeeId == null) {
                    _state.update { s ->
                        s.copy(
                            newReturnInput = s.newReturnInput.copy(
                                selectedEmployeeId = currentUserId
                            )
                        )
                    }
                }
            }
        }
        onEvent(SalesReturnScreenEvent.SearchReturns(""))
        loadDropdownData()
    }

    private fun loadDropdownData() {
        viewModelScope.launch {
            clientRepository.searchClients("")
                .collect { result -> _state.update { it.copy(availableClients = result) } }
        }
        viewModelScope.launch {
            productRepository.getProducts("")
                .collect { result -> _state.update { it.copy(availableProducts = result) } }
        }
    }

    fun onEvent(event: SalesReturnScreenEvent) {
        when (event) {
            is SalesReturnScreenEvent.SearchReturns -> searchReturns(event.query)
            is SalesReturnScreenEvent.SelectReturnToView -> _state.update {
                it.copy(
                    selectedReturn = event.salesReturn,
                    isDetailViewOpen = true
                )
            }

            is SalesReturnScreenEvent.UpdateIsQueryActive -> _state.update { it.copy(isQueryActive = event.isActive) }
            is SalesReturnScreenEvent.UpdateQuery -> _state.update { it.copy(query = event.query) }
            is SalesReturnScreenEvent.OpenNewReturnForm -> _state.update {
                it.copy(
                    isDetailViewOpen = true,
                    selectedReturn = null,
                    newReturnInput = EditableSalesReturn(selectedEmployeeId = currentUserId)
                )
            }

            is SalesReturnScreenEvent.CloseReturnForm -> _state.update {
                it.copy(
                    isDetailViewOpen = false,
                    selectedReturn = null,
                    newReturnInput = EditableSalesReturn()
                )
            }

            is SalesReturnScreenEvent.SelectClient -> updateReturnInput { it.copy(selectedClient = event.client) }
            is SalesReturnScreenEvent.UpdatePaymentType -> updateReturnInput { it.copy(paymentType = event.type) }
            is SalesReturnScreenEvent.UpdateAmountRefunded -> updateReturnInput {
                it.copy(
                    amountRefunded = event.amount
                )
            }

            is SalesReturnScreenEvent.AddItemToReturn -> updateReturnInput { it.copy(items = it.items + EditableReturnItem()) }
            is SalesReturnScreenEvent.RemoveItemFromReturn -> updateReturnInput { it.copy(items = it.items.filterNot { item -> item.tempEditorId == event.tempEditorId }) }
            is SalesReturnScreenEvent.UpdateItemProduct -> updateReturnItem(event.tempEditorId) {
                val price = event.product?.sellingPrice?.toString() ?: "0.0"
                it.copy(product = event.product, priceAtReturn = price)
            }

            is SalesReturnScreenEvent.UpdateItemUnit -> updateReturnItem(event.tempEditorId) {
                it.copy(
                    selectedUnit = event.unit
                )
            }

            is SalesReturnScreenEvent.UpdateItemQuantity -> updateReturnItem(event.tempEditorId) {
                it.copy(
                    quantity = event.quantity
                )
            }

            is SalesReturnScreenEvent.UpdateItemPrice -> updateReturnItem(event.tempEditorId) {
                it.copy(
                    priceAtReturn = event.price
                )
            }

            is SalesReturnScreenEvent.SaveReturn -> saveReturn()
            is SalesReturnScreenEvent.ClearSnackbar -> _state.update { it.copy(snackbarMessage = null) }
        }
    }

    private fun updateReturnInput(action: (EditableSalesReturn) -> EditableSalesReturn) {
        _state.update { it.copy(newReturnInput = action(it.newReturnInput)) }
        recalculateTotals()
    }

    private fun updateReturnItem(
        tempId: String, action: (EditableReturnItem) -> EditableReturnItem
    ) {
        val currentItems = _state.value.newReturnInput.items.toMutableList()
        val index = currentItems.indexOfFirst { it.tempEditorId == tempId }
        if (index != -1) {
            currentItems[index] = action(currentItems[index])
            updateReturnInput { it.copy(items = currentItems) }
        }
    }

    private fun recalculateTotals() {
        val returnInput = _state.value.newReturnInput
        var totalValue = 0.0
        var totalGainLoss = 0.0
        val updatedItems = returnInput.items.map { item ->
            val quantity = item.quantity.toDoubleOrNull() ?: 0.0
            val price = item.priceAtReturn.toDoubleOrNull() ?: 0.0
            val cost = item.product?.averagePrice ?: 0.0
            val lineTotal = quantity * price
            val lineGainLoss = (price - cost) * quantity
            totalValue += lineTotal; totalGainLoss += lineGainLoss
            item.copy(lineTotal = lineTotal, lineGainLoss = lineGainLoss)
        }
        val previousDebt = returnInput.selectedClient?.debt ?: 0.0
        val newDebt = previousDebt - totalValue
        _state.update { s ->
            s.copy(
                newReturnInput = s.newReturnInput.copy(
                    items = updatedItems,
                    totalReturnValue = totalValue,
                    totalGainLoss = totalGainLoss,
                    newDebt = newDebt
                )
            )
        }
    }

    private fun searchReturns(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, query = query) }
            delay(300)
            salesReturnRepository.getSalesReturns().catch { e ->
                _state.update {
                    it.copy(
                        loading = false, error = R.string.error_searching_orders
                    )
                }
            }.collect { returns ->
                _state.update {
                    it.copy(
                        loading = false, returns = returns
                    )
                }
            }
        }
    }

    private fun saveReturn() {
        val returnInput = _state.value.newReturnInput
        val employeeId = currentUserId ?: run {
            _state.update { it.copy(error = R.string.user_not_identified) }; return
        }
        if (returnInput.selectedClient == null || returnInput.items.isEmpty()) {
            _state.update { it.copy(error = R.string.client_and_at_least_one_item_are_required) }; return
        }
        val itemEntities = returnInput.items.mapNotNull {
            if (it.product == null || it.selectedUnit == null || (it.quantity.toDoubleOrNull()
                    ?: 0.0) <= 0
            ) return@mapNotNull null
            OrderReturnProductEntity(
                productLocalId = it.product.localId,
                unitLocalId = it.selectedUnit.localId,
                quantity = it.quantity.toDouble(),
                priceAtReturn = it.priceAtReturn.toDoubleOrNull() ?: 0.0,
                itemTotalValue = it.lineTotal,
                itemGainLoss = it.lineGainLoss,
                serverId = null,
                orderReturnLocalId = 1 // todo: this should be set after the return is saved
            )
        }
        if (itemEntities.size != returnInput.items.size) {
            _state.update { it.copy(error = R.string.one_or_more_order_items_are_invalid) }; return
        }
        val returnEntity = OrderReturnEntity(
            clientLocalId = returnInput.selectedClient.id,
            employeeLocalId = employeeId,
            previousDebt = returnInput.selectedClient.debt,
            amountPaid = returnInput.amountRefunded.toDoubleOrNull() ?: 0.0,
            amountRemaining = returnInput.newDebt - (returnInput.amountRefunded.toDoubleOrNull()
                ?: 0.0),
            totalReturnedValue = returnInput.totalReturnValue,
            totalGainLoss = returnInput.totalGainLoss,
            paymentType = returnInput.paymentType,
            returnDate = System.currentTimeMillis(),
            serverId = null,
            invoiceNumber = null,
        )
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            salesReturnRepository.addSalesReturn(returnEntity, itemEntities).fold(onSuccess = {
                _state.update {
                    it.copy(
                        loading = false,
                        isQueryActive = false,
                        snackbarMessage = R.string.return_saved
                    )
                }
            }, onFailure = { e ->
                _state.update {
                    it.copy(
                        loading = false, error = R.string.something_went_wrong
                    )
                }
            })
        }
    }
}
package com.wael.astimal.pos.features.management.presentaion.sales_return

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.compoenents.EditableItemList
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.management.data.entity.OrderReturnEntity
import com.wael.astimal.pos.features.management.data.entity.OrderReturnProductEntity
import com.wael.astimal.pos.features.management.domain.entity.EditableItem
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.SalesReturn
import com.wael.astimal.pos.features.management.domain.repository.ClientRepository
import com.wael.astimal.pos.features.management.domain.repository.SalesReturnRepository
import com.wael.astimal.pos.features.user.domain.entity.User
import com.wael.astimal.pos.features.user.domain.entity.UserType
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
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
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(SalesReturnScreenState())
    val state: StateFlow<SalesReturnScreenState> = _state.asStateFlow()
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            sessionManager.getCurrentUser().collect { updateCurrentUser(it) }
        }
        onEvent(SalesReturnScreenEvent.SearchReturns(""))
        loadDropdownData()
    }

    private fun loadDropdownData() {
        viewModelScope.launch {
            clientRepository.searchClients("")
                .collect { _state.update { it.copy(availableClients = it.availableClients + it.availableClients) } }
        }
        viewModelScope.launch {
            productRepository.getProducts("")
                .collect { _state.update { it.copy(availableProducts = it.availableProducts + it.availableProducts) } }
        }
        viewModelScope.launch {
            userRepository.getEmployeesFlow()
                .collect { _state.update { it.copy(availableEmployees = it.availableEmployees + it.availableEmployees) } }
        }
    }

    private fun updateCurrentUser(user: User?) {
        _state.update {
            when {
                user == null -> it
                user.userType == UserType.ADMIN -> it.copy(currentUser = user)
                else -> it.copy(
                    currentUser = user,
                    input = it.input.copy(selectedEmployeeId = user.id)
                )
            }
        }
    }

    fun onEvent(event: SalesReturnScreenEvent) {
        when (event) {
            is SalesReturnScreenEvent.SearchReturns -> searchReturns(event.query)
            is SalesReturnScreenEvent.SelectReturnToView -> updateSelectReturn(event.salesReturn)
            is SalesReturnScreenEvent.UpdateIsQueryActive -> _state.update { it.copy(isQueryActive = event.isActive) }
            is SalesReturnScreenEvent.UpdateQuery -> _state.update { it.copy(query = event.query) }
            is SalesReturnScreenEvent.OpenNewReturnForm -> {
                _state.update { SalesReturnScreenState(currentUser = state.value.currentUser) }
                updateCurrentUser(state.value.currentUser)
            }
            is SalesReturnScreenEvent.SelectClient -> updateReturnInput { it.copy(selectedClient = event.client) }
            is SalesReturnScreenEvent.UpdatePaymentType -> updateReturnInput { it.copy(paymentType = event.type ?: PaymentType.CASH) }
            is SalesReturnScreenEvent.UpdateAmountRefunded -> updateReturnInput { it.copy(amountPaid = event.amount) }
            is SalesReturnScreenEvent.AddItemToReturn -> updateReturnInput { it.copy(items = it.items + EditableItem()) }
            is SalesReturnScreenEvent.RemoveItemFromReturn -> updateReturnInput { it.copy(items = it.items.filterNot { item -> item.tempEditorId == event.tempEditorId }) }
            is SalesReturnScreenEvent.UpdateItemProduct -> updateReturnItem(event.tempEditorId) {
                it.copy(
                    product = event.product,
                    price = event.product?.sellingPrice?.toString() ?: "0.0",
                    selectedProductUnit = event.product?.minimumProductUnit
                )
            }
            is SalesReturnScreenEvent.UpdateItemUnit -> updateReturnItem(event.tempEditorId) { it.copy(selectedProductUnit = event.productUnit) }
            is SalesReturnScreenEvent.UpdateItemQuantity -> updateReturnItem(event.tempEditorId) { it.copy(quantity = event.quantity) }
            is SalesReturnScreenEvent.UpdateItemPrice -> updateReturnItem(event.tempEditorId) { it.copy(price = event.price) }
            is SalesReturnScreenEvent.SaveReturn -> saveReturn()
            is SalesReturnScreenEvent.ClearSnackbar -> _state.update { it.copy(snackbarMessage = null) }
            SalesReturnScreenEvent.ClearError -> _state.update { it.copy(error = null) }
            SalesReturnScreenEvent.DeleteReturn -> deleteReturn()
            is SalesReturnScreenEvent.UpdateSelectEmployee -> _state.update {
                it.copy(input = it.input.copy(selectedEmployeeId = event.id))
            }
            is SalesReturnScreenEvent.UpdateItemDate -> _state.update {
                it.copy(input = it.input.copy(date = event.date ?: System.currentTimeMillis()))
            }
        }
    }

    private fun deleteReturn() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val returnToDelete = _state.value.selectedReturn ?: return@launch
            salesReturnRepository.deleteSalesReturn(returnToDelete.localId).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            loading = false,
                            snackbarMessage = R.string.return_deleted,
                            selectedReturn = null,
                            input = EditableItemList()
                        )
                    }
                },
                onFailure = {
                    _state.update {
                        it.copy(
                            loading = false,
                            error = R.string.error_deleting_return
                        )
                    }
                }
            )
        }
    }

    private fun updateReturnInput(action: (EditableItemList) -> EditableItemList) {
        _state.update { it.copy(input = action(it.input)) }
    }

    private fun updateReturnItem(tempId: String, action: (EditableItem) -> EditableItem) {
        val currentItems = _state.value.input.items.toMutableList()
        val index = currentItems.indexOfFirst { it.tempEditorId == tempId }
        if (index != -1) {
            currentItems[index] = action(currentItems[index])
            updateReturnInput { it.copy(items = currentItems) }
        }
    }

    private fun updateSelectReturn(salesReturn: SalesReturn?) {
        _state.update {
            if (salesReturn == null) {
                it.copy(input = EditableItemList(), selectedReturn = null, isQueryActive = false)
            } else {
                it.copy(
                    selectedReturn = salesReturn,
                    input = EditableItemList(
                        selectedClient = salesReturn.client,
                        selectedEmployeeId = salesReturn.employee?.id,
                        paymentType = salesReturn.paymentType,
                        date = salesReturn.returnDate,
                        items = salesReturn.items.map { item ->
                            EditableItem(
                                tempEditorId = item.localId.toString(),
                                product = item.product,
                                selectedProductUnit = item.productUnit,
                                quantity = item.quantity.toString(),
                                price = item.priceAtReturn.toString(),
                            )
                        },
                        amountPaid = salesReturn.amountPaid.toString(),
                    ),
                    isQueryActive = false
                )
            }
        }
    }

    private fun searchReturns(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, query = query) }
            delay(300)
            salesReturnRepository.getSalesReturns()
                .catch { _state.update { it.copy(loading = false, error = R.string.error_searching_orders) } }
                .collect { returns -> _state.update { it.copy(loading = false, returns = returns) } }
        }
    }

    private fun saveReturn() {
        val returnInput = _state.value.input
        val employeeId = _state.value.currentUser?.id
        if (employeeId == null) {
            _state.update { it.copy(error = R.string.user_not_identified) }
            return
        }
        if (returnInput.selectedClient == null || returnInput.items.isEmpty()) {
            _state.update { it.copy(error = R.string.client_and_at_least_one_item_are_required) }
            return
        }
        val itemEntities = returnInput.items.mapNotNull {
            val quantity = it.quantity.toDoubleOrNull() ?: 0.0
            if (it.product == null || it.selectedProductUnit == null || quantity <= 0) return@mapNotNull null
            OrderReturnProductEntity(
                productLocalId = it.product.localId,
                unitLocalId = it.selectedProductUnit.localId,
                quantity = quantity,
                priceAtReturn = it.price.toDoubleOrNull() ?: 0.0,
                itemTotalValue = it.lineTotal,
                serverId = null,
                orderReturnLocalId = 1,
            )
        }
        if (itemEntities.size != returnInput.items.size) {
            _state.update { it.copy(error = R.string.one_or_more_order_items_are_invalid) }
            return
        }
        val returnEntity = OrderReturnEntity(
            localId = _state.value.selectedReturn?.localId ?: 0L,
            clientLocalId = returnInput.selectedClient.id,
            employeeLocalId = returnInput.selectedEmployeeId ?: employeeId,
            previousDebt = returnInput.selectedClient.debt,
            amountPaid = returnInput.amountPaid.toDoubleOrNull() ?: 0.0,
            amountRemaining = returnInput.amountRemaining,
            totalReturnedValue = returnInput.totalAmount,
            paymentType = returnInput.paymentType,
            returnDate = returnInput.date,
            serverId = null,
            invoiceNumber = null,
        )
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val result = if (_state.value.isNew) salesReturnRepository.addSalesReturn(returnEntity, itemEntities)
            else salesReturnRepository.updateSalesReturn(returnEntity, itemEntities)
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            loading = false,
                            isQueryActive = false,
                            snackbarMessage = R.string.return_saved,
                            selectedReturn = null,
                            input = EditableItemList()
                        )
                    }
                },
                onFailure = { _state.update { it.copy(loading = false, error = R.string.something_went_wrong) } }
            )
        }
    }
}
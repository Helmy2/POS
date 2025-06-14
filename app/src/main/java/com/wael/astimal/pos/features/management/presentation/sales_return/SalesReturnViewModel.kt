package com.wael.astimal.pos.features.management.presentation.sales_return

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.management.data.entity.OrderProductEntity
import com.wael.astimal.pos.features.management.data.entity.OrderReturnEntity
import com.wael.astimal.pos.features.management.domain.entity.EditableItem
import com.wael.astimal.pos.features.management.domain.entity.EditableItemList
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.SalesReturn
import com.wael.astimal.pos.features.management.domain.repository.ClientRepository
import com.wael.astimal.pos.features.management.domain.repository.SalesReturnRepository
import com.wael.astimal.pos.features.user.data.local.EmployeeDao
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SalesReturnViewModel(
    private val salesReturnRepository: SalesReturnRepository,
    private val clientRepository: ClientRepository,
    private val productRepository: ProductRepository,
    private val stockRepository: StockRepository,
    private val userRepository: UserRepository,
    private val employeeDao: EmployeeDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(SalesReturnState())
    val state: StateFlow<SalesReturnState> = _state.asStateFlow()
    private var searchJob: Job? = null
    private val stockObservationJobs = mutableMapOf<String, Job>()

    init {
        viewModelScope.launch {
            sessionManager.getCurrentUser().collect { updateCurrentUser(it) }
        }
        onEvent(SalesReturnEvent.SearchReturns(""))
        loadDropdownData()
    }

    private fun loadDropdownData() {
        viewModelScope.launch {
            clientRepository.searchClients()
                .collect { result -> _state.update { it.copy(availableClients = result) } }
        }
        viewModelScope.launch {
            productRepository.getProducts()
                .collect { result -> _state.update { it.copy(availableProducts = result) } }
        }
        viewModelScope.launch {
            userRepository.getEmployeesFlow()
                .collect { result -> _state.update { it.copy(availableEmployees = result) } }
        }
    }

    private fun updateCurrentUser(user: User?) {
        _state.update {
            when {
                user == null -> it
                user.userType == UserType.ADMIN -> it.copy(currentUser = user)
                else -> it.copy(
                    currentUser = user,
                    currentReturnInput = it.currentReturnInput.copy(selectedEmployeeId = user.id)
                )
            }
        }
    }

    fun onEvent(event: SalesReturnEvent) {
        when (event) {
            is SalesReturnEvent.SearchReturns -> searchReturns(event.query)
            is SalesReturnEvent.SelectReturnToView -> updateSelectedReturn(event.salesReturn)
            is SalesReturnEvent.SelectClient -> _state.update { it.copy(selectedClient = event.client) }
            is SalesReturnEvent.SelectEmployee -> updateReturnInput { it.copy(selectedEmployeeId = event.employeeId) }
            is SalesReturnEvent.UpdatePaymentType -> updateReturnInput {
                it.copy(paymentType = event.type ?: PaymentType.CASH)
            }
            is SalesReturnEvent.UpdateAmountPaid -> updateReturnInput { it.copy(amountPaid = event.amount) }
            is SalesReturnEvent.AddItemToReturn -> updateReturnInput { it.copy(items = it.items + EditableItem()) }
            is SalesReturnEvent.RemoveItemFromReturn -> {
                stockObservationJobs[event.tempEditorId]?.cancel()
                stockObservationJobs.remove(event.tempEditorId)
                updateReturnInput { it.copy(items = it.items.filterNot { item -> item.tempEditorId == event.tempEditorId }) }
            }
            is SalesReturnEvent.UpdateItemProduct -> {
                updateReturnItem(event.tempEditorId) {
                    val conversionFactor = event.product?.subUnitsPerMainUnit ?: 1.0
                    it.copy(
                        product = event.product,
                        minUnitPrice = (event.product?.sellingPrice?.div(conversionFactor)).toString(),
                        maxUnitPrice = event.product?.sellingPrice.toString(),
                        minUnitQuantity = conversionFactor.toString(),
                        maxUnitQuantity = "1.0",
                    )
                }
                event.product?.let {
                    observeStockForItem(event.tempEditorId, it.localId)
                }
            }
            is SalesReturnEvent.UpdateItemUnit -> updateReturnItem(event.tempEditorId) {
                it.copy(isSelectedUnitIsMax = event.isMaxUnitSelected)
            }
            is SalesReturnEvent.SaveReturn -> saveReturn()
            is SalesReturnEvent.ClearSnackbar -> _state.update { it.copy(snackbarMessage = null) }
            is SalesReturnEvent.UpdateIsQueryActive -> _state.update { it.copy(isQueryActive = event.isActive) }
            is SalesReturnEvent.UpdateQuery -> _state.update { it.copy(query = event.query) }
            is SalesReturnEvent.DeleteReturn -> deleteReturn()
            SalesReturnEvent.OpenNewReturnForm -> clearState()
            SalesReturnEvent.ClearError -> _state.update { it.copy(error = null) }
            is SalesReturnEvent.UpdateReturnDate -> updateReturnInput {
                it.copy(date = event.date ?: System.currentTimeMillis())
            }
            is SalesReturnEvent.UpdateItemMaxUnitPrice -> updateReturnItem(event.tempEditorId) {
                val conversionFactor = it.product?.subUnitsPerMainUnit ?: 1.0
                it.copy(
                    maxUnitPrice = event.price,
                    minUnitPrice = (event.price.toDoubleOrNull()?.div(conversionFactor))?.toString() ?: "0.0"
                )
            }
            is SalesReturnEvent.UpdateItemMinUnitPrice -> updateReturnItem(event.tempEditorId) {
                val conversionFactor = it.product?.subUnitsPerMainUnit ?: 1.0
                it.copy(
                    minUnitPrice = event.price,
                    maxUnitPrice = (event.price.toDoubleOrNull()?.times(conversionFactor))?.toString() ?: "0.0"
                )
            }
            is SalesReturnEvent.UpdateItemMaxUnitQuantity -> updateReturnItem(event.tempEditorId) {
                val conversionFactor = it.product?.subUnitsPerMainUnit ?: 1.0
                it.copy(
                    maxUnitQuantity = event.quantity,
                    minUnitQuantity = (event.quantity.toDoubleOrNull()?.times(conversionFactor))?.toString() ?: "0.0"
                )
            }
            is SalesReturnEvent.UpdateItemMinUnitQuantity -> updateReturnItem(event.tempEditorId) {
                val conversionFactor = it.product?.subUnitsPerMainUnit ?: 1.0
                it.copy(
                    minUnitQuantity = event.quantity,
                    maxUnitQuantity = (event.quantity.toDoubleOrNull()?.div(conversionFactor))?.toString() ?: "0.0"
                )
            }
        }
    }

    private fun observeStockForItem(tempId: String, productId: Long) {
        stockObservationJobs[tempId]?.cancel()
        viewModelScope.launch {
            val employeeId = _state.value.currentUser?.id ?: return@launch
            val storeId = employeeDao.getStoreIdForEmployee(employeeId) ?: return@launch
            stockObservationJobs[tempId] = stockRepository.getStockQuantityFlow(storeId, productId)
                .onEach { stock ->
                    updateReturnItem(tempId) { it.copy(currentStock = stock) }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun deleteReturn() {
        viewModelScope.launch {
            salesReturnRepository.deleteReturn(_state.value.selectedReturn?.localId ?: 0L)
                .fold(onSuccess = {
                    clearState(snackbarMessage = R.string.return_deleted)
                }, onFailure = {
                    _state.update {
                        it.copy(error = R.string.error_deleting_return)
                    }
                })
        }
    }

    private fun updateSelectedReturn(salesReturn: SalesReturn?) {
        stockObservationJobs.values.forEach { it.cancel() }
        stockObservationJobs.clear()

        _state.update {
            it.copy(
                isQueryActive = false,
                selectedReturn = salesReturn,
                selectedClient = salesReturn?.client,
                currentReturnInput = if (salesReturn == null) EditableItemList(
                    selectedEmployeeId = it.currentUser?.id,
                ) else EditableItemList(
                    selectedEmployeeId = salesReturn.employee?.id,
                    paymentType = salesReturn.paymentType,
                    date = salesReturn.data,
                    items = salesReturn.items.map { item ->
                        val conversionFactor = item.product?.subUnitsPerMainUnit ?: 1.0
                        EditableItem(
                            tempEditorId = item.localId.toString(),
                            product = item.product,
                            isSelectedUnitIsMax = item.productUnit?.localId == item.product?.maximumProductUnit?.localId,
                            maxUnitPrice = item.priceAtReturn.toString(),
                            minUnitPrice = (item.priceAtReturn / conversionFactor).toString(),
                            maxUnitQuantity = item.quantity.toString(),
                            minUnitQuantity = (item.quantity * conversionFactor).toString(),
                        )
                    },
                    amountPaid = salesReturn.amountPaid.toString(),
                )
            )
        }
    }

    private fun updateReturnInput(action: (EditableItemList) -> EditableItemList) {
        _state.update { it.copy(currentReturnInput = action(it.currentReturnInput)) }
    }

    private fun updateReturnItem(tempId: String, action: (EditableItem) -> EditableItem) {
        val currentItems = _state.value.currentReturnInput.items.toMutableList()
        val index = currentItems.indexOfFirst { it.tempEditorId == tempId }
        if (index != -1) {
            currentItems[index] = action(currentItems[index])
            updateReturnInput { it.copy(items = currentItems) }
        }
    }

    private fun searchReturns(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, query = query) }
            delay(300)
            salesReturnRepository.getReturns(query).catch {
                _state.update {
                    it.copy(loading = false, error = R.string.error_searching_orders)
                }
            }.collect { returns -> _state.update { it.copy(loading = false, returns = returns) } }
        }
    }

    private fun saveReturn() {
        viewModelScope.launch {
            val returnInput = _state.value.currentReturnInput
            val selectedClient = _state.value.selectedClient
            val loggedInEmployeeId = _state.value.currentUser?.id
            if (loggedInEmployeeId == null) {
                _state.update { it.copy(error = R.string.user_not_identified) }
                return@launch
            }
            if (selectedClient == null || returnInput.items.isEmpty()) {
                _state.update { it.copy(error = R.string.client_and_at_least_one_item_are_required) }
                return@launch
            }

            val itemEntities = returnInput.items.mapNotNull {
                val quantity = it.maxUnitQuantity.toDoubleOrNull() ?: 0.0
                if (it.product == null || quantity <= 0) return@mapNotNull null
                OrderProductEntity(
                    productLocalId = it.product.localId,
                    quantity = quantity,
                    unitSellingPrice = it.maxUnitPrice.toDoubleOrNull() ?: 0.0,
                    itemTotalPrice = it.lineTotal,
                    serverId = null,
                    orderLocalId = 0L
                )
            }

            if (itemEntities.size != returnInput.items.size) {
                _state.update { it.copy(error = R.string.one_or_more_order_items_are_invalid) }
                return@launch
            }

            val returnEntity = OrderReturnEntity(
                localId = _state.value.selectedReturn?.localId ?: 0L,
                serverId = null,
                invoiceNumber = null,
                clientLocalId = selectedClient.id,
                employeeLocalId = returnInput.selectedEmployeeId ?: loggedInEmployeeId,
                previousDebt = selectedClient.debt,
                amountPaid = returnInput.amountPaid.toDoubleOrNull() ?: 0.0,
                amountRemaining = returnInput.amountRemaining,
                totalAmount = returnInput.totalAmount,
                paymentType = returnInput.paymentType,
                returnDate = returnInput.date
            )

            _state.update { it.copy(loading = true) }
            val result = if (_state.value.isNew) salesReturnRepository.addReturn(returnEntity, itemEntities)
            else salesReturnRepository.updateReturn(returnEntity, itemEntities)

            result.fold(onSuccess = {
                clearState(snackbarMessage = R.string.return_saved)
            }, onFailure = {
                _state.update {
                    it.copy(loading = false, error = R.string.something_went_wrong)
                }
            })
        }
    }

    private fun clearState(snackbarMessage: Int? = null) {
        _state.update {
            it.copy(
                selectedReturn = null,
                selectedClient = null,
                currentReturnInput = EditableItemList(),
                isQueryActive = false,
                snackbarMessage = snackbarMessage,
            )
        }
        updateCurrentUser(state.value.currentUser)
    }
}

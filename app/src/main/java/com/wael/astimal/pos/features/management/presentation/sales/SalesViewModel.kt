package com.wael.astimal.pos.features.management.presentation.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.base.UiEvent
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.management.data.entity.OrderEntity
import com.wael.astimal.pos.features.management.data.entity.OrderProductEntity
import com.wael.astimal.pos.features.management.domain.entity.EditableItem
import com.wael.astimal.pos.features.management.domain.entity.EditableItemList
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.SalesOrder
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.management.domain.repository.SalesOrderRepository
import com.wael.astimal.pos.features.user.domain.entity.User
import com.wael.astimal.pos.features.user.domain.entity.UserType
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

class SalesViewModel(
    private val orderRepository: SalesOrderRepository,
    private val partnerRepository: BusinessPartnerRepository,
    private val productRepository: ProductRepository,
    private val stockRepository: StockRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OrderState())
    val state: StateFlow<OrderState> = _state.asStateFlow()
    private var searchJob: Job? = null
    private val stockObservationJobs = mutableMapOf<String, Job>()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            userRepository.getCurrentUser()?.let { updateCurrentUser(it) }
        }
        onEvent(OrderEvent.SearchOrders(""))
        loadDropdownData()
    }

    private fun loadDropdownData() {
        viewModelScope.launch {
            partnerRepository.searchClients()
                .collect { result -> _state.update { it.copy(availableClients = result) } }
        }
        viewModelScope.launch {
            productRepository.getProducts()
                .collect { result ->
                    _state.update {
                        it.copy(
                            availableProducts = result
                        )
                    }
                }
        }
        viewModelScope.launch {
            userRepository.getEmployeesFlow()
                .collect { result ->
                    _state.update {
                        it.copy(
                            availableEmployees = result
                        )
                    }
                }
        }
    }

    private fun updateCurrentUser(user: User?) {
        _state.update {
            when {
                user == null -> it
                user.userType == UserType.ADMIN -> it.copy(currentUser = user)
                else -> it.copy(
                    currentUser = user,
                    currentOrderInput = it.currentOrderInput.copy(selectedEmployeeId = user.id)
                )
            }
        }
    }

    fun onEvent(event: OrderEvent) {
        when (event) {
            is OrderEvent.SearchOrders -> searchOrders(event.query)
            is OrderEvent.SelectOrderToView -> updateSelectedOrder(event.order)
            is OrderEvent.SelectClient -> _state.update { it.copy(selectedClient = event.client) }
            is OrderEvent.SelectEmployee -> updateOrderInput { it.copy(selectedEmployeeId = event.employeeId) }
            is OrderEvent.UpdatePaymentType -> updateOrderInput {
                it.copy(paymentType = event.type ?: PaymentType.CASH)
            }

            is OrderEvent.UpdateAmountPaid -> updateOrderInput { it.copy(amountPaid = event.amount) }
            is OrderEvent.AddItemToOrder -> updateOrderInput { it.copy(items = it.items + EditableItem()) }
            is OrderEvent.RemoveItemFromOrder -> {
                stockObservationJobs[event.tempEditorId]?.cancel()
                stockObservationJobs.remove(event.tempEditorId)
                updateOrderInput { it.copy(items = it.items.filterNot { item -> item.tempEditorId == event.tempEditorId }) }
            }

            is OrderEvent.UpdateItemProduct -> {
                updateOrderItem(event.tempEditorId) {
                    val conversionFactor = event.product?.subUnitsPerMainUnit ?: 1.0
                    it.copy(
                        product = event.product,
                        minUnitPrice = (event.product?.sellingPrice?.div(conversionFactor)).toString(),
                        maxUnitPrice = event.product?.sellingPrice.toString(),
                        minUnitQuantity = conversionFactor.toString(),
                        maxUnitQuantity = "1",
                    )
                }
                event.product?.let {
                    observeStockForItem(event.tempEditorId, it.id.local)
                }
            }

            is OrderEvent.UpdateItemUnit -> updateOrderItem(event.tempEditorId) {
                it.copy(isSelectedUnitIsMax = event.isMaxUnitSelected)
            }

            is OrderEvent.SaveOrder -> saveOrder()
            is OrderEvent.UpdateIsQueryActive -> _state.update { it.copy(isQueryActive = event.isActive) }
            is OrderEvent.UpdateQuery -> _state.update { it.copy(query = event.query) }
            is OrderEvent.DeleteOrder -> deleteOrder(event.localId)
            OrderEvent.OpenNewOrderForm -> clearState()
            is OrderEvent.UpdateTransferDate -> updateOrderInput {
                it.copy(date = event.date ?: Clock.now())
            }

            is OrderEvent.UpdateItemMaxUnitPrice -> updateOrderItem(event.tempEditorId) {
                val conversionFactor = it.product?.subUnitsPerMainUnit ?: 1.0
                it.copy(
                    maxUnitPrice = event.price,
                    minUnitPrice = (event.price.toDoubleOrNull()?.div(conversionFactor))?.toString()
                        ?: "0.0"
                )
            }

            is OrderEvent.UpdateItemMinUnitPrice -> updateOrderItem(event.tempEditorId) {
                val conversionFactor = it.product?.subUnitsPerMainUnit ?: 1.0
                it.copy(
                    minUnitPrice = event.price,
                    maxUnitPrice = (event.price.toDoubleOrNull()
                        ?.times(conversionFactor))?.toString() ?: "0"
                )
            }

            is OrderEvent.UpdateItemMaxUnitQuantity -> updateOrderItem(event.tempEditorId) {
                val conversionFactor = it.product?.subUnitsPerMainUnit ?: 1.0
                it.copy(
                    maxUnitQuantity = event.quantity,
                    minUnitQuantity = (event.quantity.toDoubleOrNull()
                        ?.times(conversionFactor))?.toString() ?: "0"
                )
            }

            is OrderEvent.UpdateItemMinUnitQuantity -> updateOrderItem(event.tempEditorId) {
                val conversionFactor = it.product?.subUnitsPerMainUnit ?: 1.0
                it.copy(
                    minUnitQuantity = event.quantity,
                    maxUnitQuantity = (event.quantity.toDoubleOrNull()
                        ?.div(conversionFactor))?.toString() ?: "0"
                )
            }
        }
    }

    private fun observeStockForItem(tempId: String, productId: Long) {
        stockObservationJobs[tempId]?.cancel()
        viewModelScope.launch {
            val employeeId = _state.value.currentOrderInput.selectedEmployeeId ?: return@launch
            val storeId =
                userRepository.getStoreIdForEmployee(employeeId).getOrNull() ?: return@launch
            stockObservationJobs[tempId] =
                stockRepository.getStockQuantityFlow(storeId, productId).onEach { stock ->
                    updateOrderItem(tempId) { it.copy(currentStock = stock) }
                }.launchIn(viewModelScope)
        }
    }

    private fun deleteOrder(id: Long) {
        viewModelScope.launch {
            orderRepository.deleteOrder(id).fold(onSuccess = {
                clearState(snackbarMessage = R.string.order_deleted)
            }, onFailure = {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_deleting_order))
            })
        }
    }

    private fun updateSelectedOrder(order: SalesOrder?) {
        stockObservationJobs.values.forEach { it.cancel() }
        stockObservationJobs.clear()

        _state.update {
            it.copy(
                isQueryActive = false,
                selectedOrder = order,
                selectedClient = order?.client,
                currentOrderInput = if (order == null) EditableItemList(
                    selectedEmployeeId = it.currentUser?.id,
                ) else EditableItemList(
                    selectedEmployeeId = order.employee.id,
                    paymentType = order.paymentType,
                    date = order.createdAt,
                    items = order.items.map { item ->
                        val conversionFactor = item.product.subUnitsPerMainUnit
                        EditableItem(
                            tempEditorId = item.id.local.toString(),
                            product = item.product,
                            isSelectedUnitIsMax = true,
                            maxUnitPrice = item.unitSellingPrice.toString(),
                            minUnitPrice = (item.unitSellingPrice / conversionFactor).toString(),
                            maxUnitQuantity = item.quantity.toString(),
                            minUnitQuantity = (item.quantity * conversionFactor).toString(),
                        )
                    },
                    amountPaid = order.amountPaid.toString(),
                )
            )
        }
    }

    private fun updateOrderInput(action: (EditableItemList) -> EditableItemList) {
        _state.update { it.copy(currentOrderInput = action(it.currentOrderInput)) }
    }

    private fun updateOrderItem(tempId: String, action: (EditableItem) -> EditableItem) {
        val currentItems = _state.value.currentOrderInput.items.toMutableList()
        val index = currentItems.indexOfFirst { it.tempEditorId == tempId }
        if (index != -1) {
            currentItems[index] = action(currentItems[index])
            updateOrderInput { it.copy(items = currentItems) }
        }
    }

    private fun searchOrders(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, query = query) }
            delay(300)
            orderRepository.getOrders(query).catch {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.error_searching_orders))
            }.collect { orders -> _state.update { it.copy(loading = false, orders = orders) } }
        }
    }

    private fun saveOrder() {
        viewModelScope.launch {
            val orderInput = _state.value.currentOrderInput
            val selectedClient = _state.value.selectedClient
            val loggedInEmployeeId = _state.value.currentUser?.id
            if (loggedInEmployeeId == null) {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.user_not_identified))
                return@launch
            }
            if (selectedClient == null || orderInput.items.isEmpty()) {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.client_and_at_least_one_item_are_required))
                return@launch
            }

            val itemEntities = orderInput.items.mapNotNull {
                val quantity = it.maxUnitQuantity.toDoubleOrNull() ?: 0.0
                if (it.product == null || quantity <= 0) return@mapNotNull null

                if (quantity > it.currentStock) {
                    _eventFlow.emit(UiEvent.ShowSnackbar(R.string.not_enough_stock))
                    return@launch
                }

                OrderProductEntity(
                    productLocalId = it.product.id.local,
                    quantity = quantity,
                    unitSellingPrice = it.maxUnitPrice.toDoubleOrNull() ?: 0.0,
                    itemTotalPrice = it.lineTotal,
                    serverId = null,
                    orderLocalId = 0L
                )
            }

            if (itemEntities.size != orderInput.items.size) {
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.one_or_more_order_items_are_invalid))
                return@launch
            }

            val orderEntity = OrderEntity(
                localId = _state.value.selectedOrder?.id?.local ?: 0L,
                serverId = null,
                invoiceNumber = "",
                clientLocalId = selectedClient.clientId?.local ?: throw Exception(),
                employeeLocalId = orderInput.selectedEmployeeId ?: loggedInEmployeeId,
                amountPaid = orderInput.amountPaid.toDoubleOrNull() ?: 0.0,
                amountRemaining = orderInput.amountRemaining,
                totalAmount = orderInput.totalAmount,
                paymentType = orderInput.paymentType,
                createdAt = orderInput.date
            )

            _state.update { it.copy(loading = true) }
            val result = if (_state.value.isNew) orderRepository.addOrder(orderEntity, itemEntities)
            else orderRepository.updateOrder(orderEntity, itemEntities)

            result.fold(onSuccess = {
                clearState(snackbarMessage = R.string.order_saved)
            }, onFailure = { e ->
                _eventFlow.emit(UiEvent.ShowSnackbar(R.string.something_went_wrong))
            })
        }
    }

    private fun clearState(snackbarMessage: Int? = null) {
        _state.update {
            it.copy(
                selectedOrder = null,
                selectedClient = null,
                currentOrderInput = EditableItemList(),
                isQueryActive = false,
            )
        }
        updateCurrentUser(state.value.currentUser)
        viewModelScope.launch {
            snackbarMessage?.let {
                _eventFlow.emit(UiEvent.ShowSnackbar(snackbarMessage))
            }
        }
    }
}

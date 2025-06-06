package com.wael.astimal.pos.features.client_management.presentaion.sales_order

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.features.client_management.data.entity.OrderEntity
import com.wael.astimal.pos.features.client_management.data.entity.OrderProductEntity
import com.wael.astimal.pos.features.client_management.domain.repository.ClientRepository
import com.wael.astimal.pos.features.client_management.domain.repository.SalesOrderRepository
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
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

class SalesOrderViewModel(
    private val orderRepository: SalesOrderRepository,
    private val clientRepository: ClientRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(OrderScreenState())
    val state: StateFlow<OrderScreenState> = _state.asStateFlow()
    private var searchJob: Job? = null
    private var currentUserId: Long? = null

    init {
        viewModelScope.launch {
            sessionManager.getCurrentUser().collect {
                currentUserId = it?.id?.toLong()
                if (_state.value.currentOrderInput.selectedEmployeeId == null) {
                    _state.update { s ->
                        s.copy(
                            currentOrderInput = s.currentOrderInput.copy(
                                selectedEmployeeId = currentUserId
                            )
                        )
                    }
                }
            }
        }
        viewModelScope.launch {
            onEvent(OrderScreenEvent.SearchOrders(""))
        }
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
        viewModelScope.launch {
            userRepository.getEmployeesFlow()
                .collect { result -> _state.update { it.copy(availableEmployees = result) } }
        }
    }

    fun onEvent(event: OrderScreenEvent) {
        when (event) {
            is OrderScreenEvent.SearchOrders -> searchOrders(event.query)
            is OrderScreenEvent.SelectOrderToView -> _state.update {
                it.copy(selectedOrder = event.order, isDetailViewOpen = true)
            }

            is OrderScreenEvent.SelectClient -> updateOrderInput { it.copy(selectedClient = event.client) }
            is OrderScreenEvent.SelectEmployee -> updateOrderInput {
                it.copy(selectedMainEmployeeId = event.employeeId)
            }

            is OrderScreenEvent.UpdatePaymentType -> updateOrderInput { it.copy(paymentType = event.type) }
            is OrderScreenEvent.UpdateAmountPaid -> updateOrderInput { it.copy(amountPaid = event.amount) }
            is OrderScreenEvent.AddItemToOrder -> updateOrderInput { it.copy(items = it.items + EditableOrderItem()) }
            is OrderScreenEvent.RemoveItemFromOrder -> updateOrderInput { it.copy(items = it.items.filterNot { item -> item.tempEditorId == event.tempEditorId }) }
            is OrderScreenEvent.UpdateItemProduct -> updateOrderItem(event.tempEditorId) {
                val price = event.product?.sellingPrice?.toString() ?: "0.0"
                it.copy(product = event.product, sellingPrice = price)
            }

            is OrderScreenEvent.UpdateItemUnit -> updateOrderItem(event.tempEditorId) {
                it.copy(
                    selectedUnit = event.unit
                )
            }

            is OrderScreenEvent.UpdateItemQuantity -> updateOrderItem(event.tempEditorId) {
                it.copy(
                    quantity = event.quantity
                )
            }

            is OrderScreenEvent.UpdateItemPrice -> updateOrderItem(event.tempEditorId) {
                it.copy(sellingPrice = event.price)
            }

            is OrderScreenEvent.SaveOrder -> saveOrder()
            is OrderScreenEvent.ClearSnackbar -> _state.update { it.copy(snackbarMessage = null) }

            is OrderScreenEvent.UpdateIsQueryActive -> _state.update { it.copy(isQueryActive = event.isActive) }

            is OrderScreenEvent.UpdateQuery -> _state.update { it.copy(query = event.query) }
            OrderScreenEvent.CloseOrderForm -> _state.update {
                it.copy(
                    isDetailViewOpen = false,
                    selectedOrder = null,
                    currentOrderInput = EditableOrder(),
                    error = null
                )
            }
            is OrderScreenEvent.DeleteOrder -> {
                // todo: Implement delete order logic
            }
            OrderScreenEvent.OpenNewOrderForm -> _state.update {
                it.copy(
                    isDetailViewOpen = false,
                    selectedOrder = null,
                    currentOrderInput = EditableOrder(
                        selectedEmployeeId = currentUserId ?: 0L
                    ),
                    error = null
                )
            }
        }
    }

    private fun updateOrderInput(action: (EditableOrder) -> EditableOrder) {
        _state.update { it.copy(currentOrderInput = action(it.currentOrderInput)) }
        recalculateTotals()
    }

    private fun updateOrderItem(tempId: String, action: (EditableOrderItem) -> EditableOrderItem) {
        val currentItems = _state.value.currentOrderInput.items.toMutableList()
        val index = currentItems.indexOfFirst { it.tempEditorId == tempId }
        if (index != -1) {
            currentItems[index] = action(currentItems[index])
            updateOrderInput { it.copy(items = currentItems) }
        }
    }

    private fun recalculateTotals() {
        val orderInput = _state.value.currentOrderInput
        var subtotal = 0.0
        var totalGain = 0.0
        val updatedItems = orderInput.items.map { item ->
            val quantity = item.quantity.toDoubleOrNull() ?: 0.0
            val price = item.sellingPrice.toDoubleOrNull() ?: 0.0
            val cost = item.product?.averagePrice ?: 0.0
            val lineTotal = quantity * price
            val lineGain = if (price > cost) (price - cost) * quantity else 0.0
            subtotal += lineTotal; totalGain += lineGain
            item.copy(lineTotal = lineTotal, lineGain = lineGain)
        }
        val previousDebt = orderInput.selectedClient?.debt ?: 0.0
        val totalAmount = subtotal + previousDebt
        val amountPaid = orderInput.amountPaid.toDoubleOrNull() ?: 0.0
        val amountRemaining = totalAmount - amountPaid
        _state.update { s ->
            s.copy(
                currentOrderInput = s.currentOrderInput.copy(
                    items = updatedItems,
                    subtotal = subtotal,
                    totalGain = totalGain,
                    totalAmount = totalAmount,
                    amountRemaining = amountRemaining
                )
            )
        }
    }

    private fun searchOrders(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, query = query) }
            delay(300)
            orderRepository.getOrders(query).catch { e ->
                _state.update {
                    it.copy(
                        loading = false, error = "Error searching orders: ${e.message}"
                    )
                }
            }.collect { orders -> _state.update { it.copy(loading = false, orders = orders) } }
        }
    }

    private fun saveOrder() {
        Log.d("TAG", "saveOrder: Starting save order process")

        val orderInput = _state.value.currentOrderInput
        Log.d("TAG", "saveOrder: Order Input: $currentUserId")
        val loggedInEmployeeId = currentUserId ?: run {
            _state.update { it.copy(error = "User not identified.") }
            return
        }
        if (orderInput.selectedClient == null || orderInput.items.isEmpty()) {
            _state.update { it.copy(error = "Client and at least one item are required.") }
            return
        }
        val itemEntities = orderInput.items.mapNotNull {
            if (it.product == null || it.selectedUnit == null || (it.quantity.toDoubleOrNull()
                    ?: 0.0) <= 0
            ) return@mapNotNull null
            OrderProductEntity(
                productLocalId = it.product.localId,
                unitLocalId = it.selectedUnit.localId,
                quantity = it.quantity.toDouble(),
                unitSellingPrice = it.sellingPrice.toDoubleOrNull() ?: 0.0,
                itemTotalPrice = it.lineTotal,
                itemGain = it.lineGain,
                localId = 0L,
                serverId = null,
                orderLocalId = 0L
            )
        }
        if (itemEntities.size != orderInput.items.size) {
            _state.update { it.copy(error = "One or more order items are invalid.") }
            return
        }
        val orderEntity = OrderEntity(
            clientLocalId = orderInput.selectedClient.localId,
            employeeLocalId = loggedInEmployeeId,
            mainEmployeeLocalId = orderInput.selectedMainEmployeeId,
            previousClientDebt = orderInput.selectedClient.debt,
            amountPaid = orderInput.amountPaid.toDoubleOrNull() ?: 0.0,
            amountRemaining = orderInput.amountRemaining,
            totalPrice = orderInput.subtotal,
            totalGain = orderInput.totalGain,
            paymentType = orderInput.paymentType,
            orderDate = System.currentTimeMillis(),
            serverId = null,
            invoiceNumber = null,
        )
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            orderRepository.addOrder(orderEntity, itemEntities).fold(
                onSuccess = {
                _state.update {
                    it.copy(
                        loading = false, snackbarMessage = "Order Saved"
                    )
                }
            },
                onFailure = { e -> _state.update { it.copy(loading = false, error = e.message) } })
        }
    }
}
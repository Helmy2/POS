package com.wael.astimal.pos.features.management.presentaion.sales

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.compoenents.EditableItemList
import com.wael.astimal.pos.features.management.data.entity.OrderEntity
import com.wael.astimal.pos.features.management.data.entity.OrderProductEntity
import com.wael.astimal.pos.features.management.domain.entity.SalesOrder
import com.wael.astimal.pos.features.management.domain.repository.ClientRepository
import com.wael.astimal.pos.features.management.domain.repository.SalesOrderRepository
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.management.domain.entity.EditableItem
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
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

class SalesViewModel(
    private val orderRepository: SalesOrderRepository,
    private val clientRepository: ClientRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(OrderState())
    val state: StateFlow<OrderState> = _state.asStateFlow()
    private var searchJob: Job? = null
    private var currentUserId: Long? = null

    init {
        viewModelScope.launch {
            sessionManager.getCurrentUser().collect {
                currentUserId = it?.id
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
            onEvent(OrderEvent.SearchOrders(""))
        }
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

    fun onEvent(event: OrderEvent) {
        when (event) {
            is OrderEvent.SearchOrders -> searchOrders(event.query)
            is OrderEvent.SelectOrderToView -> updateSelectedOrder(event.order)

            is OrderEvent.SelectClient -> updateOrderInput { it.copy(selectedClient = event.client) }
            is OrderEvent.SelectEmployee -> updateOrderInput {
                it.copy(selectedEmployeeId = event.employeeId)
            }

            is OrderEvent.UpdatePaymentType -> updateOrderInput { it.copy(paymentType = event.type ?: PaymentType.CASH) }
            is OrderEvent.UpdateAmountPaid -> updateOrderInput { it.copy(amountPaid = event.amount) }
            is OrderEvent.AddItemToOrder -> updateOrderInput { it.copy(items = it.items + EditableItem()) }
            is OrderEvent.RemoveItemFromOrder -> updateOrderInput { it.copy(items = it.items.filterNot { item -> item.tempEditorId == event.tempEditorId }) }
            is OrderEvent.UpdateItemProduct -> updateOrderItem(event.tempEditorId) {
                val price = event.product?.sellingPrice?.toString() ?: "0.0"
                it.copy(product = event.product, price = price)
            }

            is OrderEvent.UpdateItemUnit -> updateOrderItem(event.tempEditorId) {
                it.copy(
                    selectedProductUnit = event.productUnit
                )
            }

            is OrderEvent.UpdateItemQuantity -> updateOrderItem(event.tempEditorId) {
                it.copy(
                    quantity = event.quantity
                )
            }

            is OrderEvent.UpdateItemPrice -> updateOrderItem(event.tempEditorId) {
                it.copy(price = event.price)
            }

            is OrderEvent.SaveOrder -> saveOrder()
            is OrderEvent.ClearSnackbar -> _state.update { it.copy(snackbarMessage = null) }

            is OrderEvent.UpdateIsQueryActive -> _state.update { it.copy(isQueryActive = event.isActive) }

            is OrderEvent.UpdateQuery -> _state.update { it.copy(query = event.query) }
            is OrderEvent.DeleteOrder -> deleteOrder(event.localId)

            OrderEvent.OpenNewOrderForm -> _state.update {
                it.copy(
                    isQueryActive = false, selectedOrder = null, currentOrderInput = EditableItemList(
                        selectedEmployeeId = currentUserId ?: 0L
                    ), error = null
                )
            }

            OrderEvent.ClearError -> _state.update {
                it.copy(error = null)
            }

            is OrderEvent.UpdateTransferDate -> updateOrderInput {
                it.copy(date = event.date ?: System.currentTimeMillis())
            }
        }
    }

    private fun deleteOrder(id: Long) {
        viewModelScope.launch {
            val result = orderRepository.deleteOrder(id)

            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            snackbarMessage = R.string.order_deleted,
                            selectedOrder = null,
                            currentOrderInput = EditableItemList(),
                            isQueryActive = false
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(error = R.string.error_deleting_order) }
                }
            )
        }

    }

    private fun updateSelectedOrder(order: SalesOrder?) {
        _state.update {
            it.copy(
                selectedOrder = order, isQueryActive = false
            )
        }
        if (order == null) {
            _state.update { it.copy(currentOrderInput = EditableItemList()) }
        } else {
            _state.update {
                it.copy(
                    currentOrderInput = EditableItemList(
                        selectedClient = order.client,
                        selectedEmployeeId = order.employee?.id,
                        paymentType = order.paymentType,
                        date = order.orderDate,
                        items = order.items.map {
                            EditableItem(
                                tempEditorId = it.localId.toString(),
                                product = it.product,
                                selectedProductUnit = it.productUnit,
                                quantity = it.quantity.toString(),
                                price = it.unitSellingPrice.toString(),
                                lineTotal = it.itemTotalPrice,
                                lineGain = it.itemGain
                            )
                        },
                        amountPaid = order.amountPaid.toString(),
                        subtotal = order.totalPrice,
                        totalGain = order.totalGain,
                        totalAmount = order.totalPrice + (order.client?.debt ?: 0.0),
                        amountRemaining = order.amountRemaining
                    )
                )
            }
        }
    }

    private fun updateOrderInput(action: (EditableItemList) -> EditableItemList) {
        _state.update { it.copy(currentOrderInput = action(it.currentOrderInput)) }
        recalculateTotals()
    }

    private fun updateOrderItem(tempId: String, action: (EditableItem) -> EditableItem) {
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
            val price = item.price.toDoubleOrNull() ?: 0.0
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
                        loading = false, error = R.string.error_searching_orders
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
            _state.update { it.copy(error = R.string.user_not_identified) }
            return
        }
        if (orderInput.selectedClient == null || orderInput.items.isEmpty()) {
            _state.update { it.copy(error = R.string.client_and_at_least_one_item_are_required) }
            return
        }
        val itemEntities = orderInput.items.mapNotNull {
            if (it.product == null || it.selectedProductUnit == null || (it.quantity.toDoubleOrNull()
                    ?: 0.0) <= 0
            ) return@mapNotNull null
            OrderProductEntity(
                productLocalId = it.product.localId,
                unitLocalId = it.selectedProductUnit.localId,
                quantity = it.quantity.toDouble(),
                unitSellingPrice = it.price.toDoubleOrNull() ?: 0.0,
                itemTotalPrice = it.lineTotal,
                itemGain = it.lineGain,
                serverId = null,
                orderLocalId = 0L
            )
        }
        if (itemEntities.size != orderInput.items.size) {
            _state.update { it.copy(error = R.string.one_or_more_order_items_are_invalid) }
            return
        }
        val orderEntity = OrderEntity(
            clientLocalId = orderInput.selectedClient.id,
            employeeLocalId = orderInput.selectedEmployeeId ?: loggedInEmployeeId,
            previousClientDebt = orderInput.selectedClient.debt,
            amountPaid = orderInput.amountPaid.toDoubleOrNull() ?: 0.0,
            amountRemaining = orderInput.amountRemaining,
            totalPrice = orderInput.subtotal,
            totalGain = orderInput.totalGain,
            paymentType = orderInput.paymentType,
            orderDate = orderInput.date,
            serverId = null,
            localId = state.value.selectedOrder?.localId ?: 0L,
            invoiceNumber = null,
        )
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }

            val result = if (_state.value.isNew) orderRepository.addOrder(orderEntity, itemEntities)
            else orderRepository.updateOrder(orderEntity, itemEntities)

            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            loading = false,
                            snackbarMessage = R.string.order_saved,
                            isQueryActive = false,
                            selectedOrder = null,
                            currentOrderInput = EditableItemList(),
                        )
                    }
                },
                onFailure = { e ->
                    Log.d("TAG", "saveOrder: $e")
                    _state.update {
                        it.copy(
                            loading = false, error = R.string.something_went_wrong
                        )
                    }
                },
            )
        }
    }
}
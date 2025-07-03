package com.wael.astimal.pos.features.management.presentation.sales

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.management.domain.entity.SalesOrder
import com.wael.astimal.pos.features.management.domain.entity.SalesOrderItem
import com.wael.astimal.pos.features.management.domain.entity.matchesQuery
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.management.domain.repository.SalesOrderRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SalesViewModel(
    private val salesOrderRepository: SalesOrderRepository,
    private val partnerRepository: BusinessPartnerRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val stockRepository: StockRepository,
    private val snackbarController: SnackbarController,
    private val navigationController: NavigationController,
) : BaseViewModel<SalesContract.State, SalesContract.Event, Nothing>(
    reducer = SalesReducer(),
    initialState = SalesContract.State(
        currentOrderInput = SalesContract.EditableOrder(date = Clock.now())
    )
) {
    private val stockObservationJobs = mutableMapOf<String, Job>()

    val filteredOrdersState: StateFlow<List<SalesOrder>> =
        combine(
            state,
            salesOrderRepository.getOrders()
        ) { state, allOrders ->
            if (state.orders != allOrders) {
                setState(SalesContract.Event.OrdersLoaded(allOrders))
            }
            if (state.searchQuery.isBlank()) {
                allOrders
            } else {
                allOrders.filter { it.matchesQuery(state.searchQuery) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    override fun handleEvent(event: SalesContract.Event) {
        when (event) {
            is SalesContract.Event.SaveClicked -> {
                stockObservationJobs.values.forEach { it.cancel() }
                stockObservationJobs.clear()
                saveOrder()
            }

            is SalesContract.Event.DeleteClicked -> {
                stockObservationJobs.values.forEach { it.cancel() }
                stockObservationJobs.clear()
                deleteOrder()
            }

            is SalesContract.Event.BackClicked -> {
                stockObservationJobs.values.forEach { it.cancel() }
                stockObservationJobs.clear()
                navigateBack()
            }

            is SalesContract.Event.RemoveItem -> {
                stockObservationJobs[event.editorId]?.cancel()
                stockObservationJobs.remove(event.editorId)
                setState(event)
            }

            is SalesContract.Event.ClientSelected -> {
                viewModelScope.launch {
                    event.client?.let {
                        val balance = partnerRepository.getPartnerBalance(it).getOrDefault(0.0)
                        setState(SalesContract.Event.PartnerBalanceChanged(balance))
                    }
                    setState(event)
                }
            }

            is SalesContract.Event.ItemProductChanged -> {
                event.product?.let {
                    observeStockForItem(event.editorId, it.id.local)
                }
                setState(event)
            }

            else -> setState(event)
        }
    }

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            setState(SalesContract.Event.UserLoaded(userRepository.getCurrentUser()))
        }
        viewModelScope.launch {
            combine(
                partnerRepository.getClients(""),
                productRepository.getProducts(""),
                userRepository.getEmployeesFlow()
            ) { clients, products, employees ->
                SalesContract.DropdownData(clients, products, employees)
            }.collect {
                setState(SalesContract.Event.DropdownDataLoaded(it))
            }
        }
    }

    private fun saveOrder() {
        viewModelScope.launch {
            val currentState = state.value
            if (!currentState.canSave || currentState.selectedClient == null || currentState.currentOrderInput.selectedEmployee == null) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.error_some_field_are_required)))
                return@launch
            }
            setState(SalesContract.Event.LoadingStarted)

            val orderItems = currentState.currentOrderInput.items.mapNotNull {
                val quantity = it.maxUnitQuantity.toDoubleOrNull()
                val price = it.maxUnitPrice.toDoubleOrNull()
                if (it.product != null && quantity != null && quantity > 0 && price != null) {
                    SalesOrderItem(
                        id = Id.new,
                        product = it.product,
                        quantity = quantity,
                        unitSellingPrice = price,
                        itemTotalPrice = it.lineTotal,
                    )
                } else null
            }

            if (orderItems.size != currentState.currentOrderInput.items.size) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.one_or_more_order_items_are_invalid)))
                setState(SalesContract.Event.LoadingFinished)
                return@launch
            }

            val orderToSave = SalesOrder(
                client = currentState.selectedClient,
                employee = currentState.currentOrderInput.selectedEmployee,
                paymentType = currentState.currentOrderInput.paymentType,
                amountPaid = currentState.currentOrderInput.amountPaid.toDoubleOrNull() ?: 0.0,
                createdAt = currentState.selectedOrder?.createdAt ?: Clock.now(),
                items = orderItems,
                isSynced = false,
                totalAmount = currentState.currentOrderInput.totalAmount,
                updatedAt = currentState.selectedOrder?.updatedAt ?: Clock.now(),
                id = currentState.selectedOrder?.id ?: Id.new,
                invoiceNumber = currentState.selectedOrder?.invoiceNumber ?: "",
                amountRemaining = currentState.currentOrderInput.amountRemaining,
                orderDate = currentState.currentOrderInput.date
            )

            val result = if (currentState.isEditing) {
                salesOrderRepository.updateOrder(orderToSave)
            } else {
                salesOrderRepository.addOrder(orderToSave)
            }

            result.onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.order_saved)))
                setState(SalesContract.Event.SaveSucceeded)
            }.onFailure {
                it.printStackTrace()
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.something_went_wrong)))
                setState(SalesContract.Event.LoadingFinished)
            }
        }
    }

    private fun deleteOrder() {
        setState(SalesContract.Event.LoadingStarted)
        viewModelScope.launch {
            state.value.selectedOrder?.id?.local?.let {

                salesOrderRepository.deleteOrder(it).onSuccess {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.order_deleted)))
                    setState(SalesContract.Event.DeleteSucceeded)
                }.onFailure {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.error_deleting_order)))
                    setState(SalesContract.Event.LoadingFinished)
                }
            }
                ?: snackbarController.sendEvent(
                    SnackbarEvent(StringResource.FromResource(R.string.error_no_order_selected))
                )
        }
    }

    private fun observeStockForItem(tempId: String, productId: Long) {
        stockObservationJobs[tempId]?.cancel()
        viewModelScope.launch {
            val employeeId = state.value.currentOrderInput.selectedEmployee?.id ?: return@launch
            val storeId =
                userRepository.getStoreIdForEmployee(employeeId).getOrNull() ?: return@launch
            stockObservationJobs[tempId] =
                stockRepository.getStockQuantityFlow(storeId, productId)
                    .catch {
                        snackbarController.sendEvent(
                            SnackbarEvent(StringResource.FromResource(R.string.error_fetching_stock))
                        )
                    }
                    .onEach { stock ->
                        setState(SalesContract.Event.ItemStockChanged(tempId, stock))
                    }.launchIn(viewModelScope)
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            navigationController.navigateBack()
        }
    }
}

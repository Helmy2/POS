package com.wael.astimal.pos.features.management.presentation.sales

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceType
import com.wael.astimal.pos.features.management.data.local.entity.PaymentMethod
import com.wael.astimal.pos.features.management.domain.entity.Invoice
import com.wael.astimal.pos.features.management.domain.entity.InvoiceItem
import com.wael.astimal.pos.features.management.domain.entity.SalesOrder
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.management.domain.repository.InvoiceRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pos.app.generated.resources.Res
import pos.app.generated.resources.error_some_field_are_required
import pos.app.generated.resources.one_or_more_order_items_are_invalid
import pos.app.generated.resources.order_saved
import pos.app.generated.resources.something_went_wrong

class SalesViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val partnerRepository: BusinessPartnerRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val stockRepository: StockRepository,
    private val storeRepository: StoreRepository,
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
        MutableStateFlow(emptyList())

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
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_some_field_are_required)))
                return@launch
            }
            setState(SalesContract.Event.LoadingStarted)

            val orderItems = currentState.currentOrderInput.items.mapNotNull {
                val quantity = it.maxUnitQuantity.toDoubleOrNull()
                val price = it.maxUnitPrice.toDoubleOrNull()
                if (it.product != null && quantity != null && quantity > 0 && price != null) {
                    InvoiceItem(
                        id = Id.new,
                        product = it.product,
                        quantity = quantity,
                        unitPrice = price,
                        isSynced = false
                    )
                } else null
            }

            if (orderItems.size != currentState.currentOrderInput.items.size) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.one_or_more_order_items_are_invalid)))
                setState(SalesContract.Event.LoadingFinished)
                return@launch
            }

            val orderToSave = Invoice(
                partner = currentState.selectedClient,
                employee = currentState.currentOrderInput.selectedEmployee,
                paidAmount = currentState.currentOrderInput.amountPaid.toDoubleOrNull() ?: 0.0,
                createdAt = currentState.selectedOrder?.createdAt ?: Clock.now(),
                items = orderItems,
                isSynced = false,
                totalAmount = currentState.currentOrderInput.totalAmount,
                updatedAt = currentState.selectedOrder?.updatedAt ?: Clock.now(),
                id = currentState.selectedOrder?.id ?: Id.new,
                orderDate = currentState.currentOrderInput.date,
                invoiceType = InvoiceType.SALES,
                paymentMethod = PaymentMethod.CARD,
                store = storeRepository.getStores().first().first()
            )

            val result = invoiceRepository.addSalesOrder(
                orderToSave
            )

//            val result = if (currentState.isEditing) {
//                salesOrderRepository.updateOrder(orderToSave)
//            } else {
//                salesOrderRepository.addOrder(orderToSave)
//            }
//
            result.onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.order_saved)))
                setState(SalesContract.Event.SaveSucceeded)
            }.onFailure {
                it.printStackTrace()
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.something_went_wrong)))
                setState(SalesContract.Event.LoadingFinished)
            }
        }
    }

    private fun deleteOrder() {

    }

    private fun observeStockForItem(tempId: String, productId: Long) {
//        stockObservationJobs[tempId]?.cancel()
//        viewModelScope.launch {
//            val employeeId = state.value.currentOrderInput.selectedEmployee?.id ?: return@launch
//            val storeId =
//                userRepository.getStoreIdForEmployee(employeeId.local).getOrNull() ?: return@launch
//            stockObservationJobs[tempId] =
//                stockRepository.getStockQuantityFlow(storeId, productId)
//                    .catch {
//                        snackbarController.sendEvent(
//                            SnackbarEvent(StringResource.FromResource(Res.string.error_fetching_stock))
//                        )
//                    }
//                    .onEach { stock ->
//                        setState(SalesContract.Event.ItemStockChanged(tempId, stock))
//                    }.launchIn(viewModelScope)
//        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            navigationController.navigateBack()
        }
    }
}

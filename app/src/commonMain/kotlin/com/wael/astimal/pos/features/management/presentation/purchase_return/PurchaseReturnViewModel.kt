package com.wael.astimal.pos.features.management.presentation.purchase_return

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceType
import com.wael.astimal.pos.features.management.domain.entity.Invoice
import com.wael.astimal.pos.features.management.domain.entity.InvoiceItem
import com.wael.astimal.pos.features.management.domain.entity.matchesQuery
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.management.domain.repository.InvoiceRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pos.app.generated.resources.Res
import pos.app.generated.resources.error_deleting_order
import pos.app.generated.resources.error_fetching_stock
import pos.app.generated.resources.error_no_order_selected
import pos.app.generated.resources.error_some_field_are_required
import pos.app.generated.resources.invalid_item_with_args
import pos.app.generated.resources.one_or_more_order_items_are_invalid
import pos.app.generated.resources.order_deleted
import pos.app.generated.resources.order_saved
import pos.app.generated.resources.something_went_wrong

class PurchaseReturnViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val partnerRepository: BusinessPartnerRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val stockRepository: StockRepository,
    private val storeRepository: StoreRepository,
    private val snackbarController: SnackbarController,
    private val navigationController: NavigationController,
) : BaseViewModel<PurchaseReturnContract.State, PurchaseReturnContract.Event, Nothing>(
    reducer = PurchaseReturnReducer(),
    initialState = PurchaseReturnContract.State(
        currentOrderInput = PurchaseReturnContract.EditableOrder(date = Clock.now())
    )
) {

    private val stockObservationJobs = mutableMapOf<String, Job>()

    val filteredOrdersState: StateFlow<List<Invoice>> = combine(
        state, invoiceRepository.getInvoices().map { list ->
            list.filter { it.invoiceType == InvoiceType.PURCHASE_RETURN }
        }
    ) { state, allOrders ->
        if (state.orders != allOrders) {
            setState(PurchaseReturnContract.Event.OrdersLoaded(allOrders))
        }
        if (state.searchQuery.isBlank()) {
            allOrders
        } else {
            allOrders.filter { it.matchesQuery(state.searchQuery) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    override fun handleEvent(event: PurchaseReturnContract.Event) {
        when (event) {
            is PurchaseReturnContract.Event.SaveClicked -> {
                stockObservationJobs.values.forEach { it.cancel() }
                stockObservationJobs.clear()
                saveOrder()
            }

            is PurchaseReturnContract.Event.DeleteClicked -> {
                stockObservationJobs.values.forEach { it.cancel() }
                stockObservationJobs.clear()
                deleteOrder()
            }

            is PurchaseReturnContract.Event.BackClicked -> {
                stockObservationJobs.values.forEach { it.cancel() }
                stockObservationJobs.clear()
                navigateBack()
            }

            is PurchaseReturnContract.Event.RemoveItem -> {
                stockObservationJobs[event.editorId]?.cancel()
                stockObservationJobs.remove(event.editorId)
                setState(event)
            }

            is PurchaseReturnContract.Event.PartnerSelected -> {
                viewModelScope.launch {
                    event.partner?.let {
                        val balance = partnerRepository.getPartnerBalance(it).getOrDefault(0.0)
                        setState(PurchaseReturnContract.Event.PartnerBalanceChanged(balance))
                    }
                    setState(event)
                }
            }

            is PurchaseReturnContract.Event.ItemProductChanged -> {
                event.product?.let { observeStockForItem(event.editorId, it.id) }
                setState(event)
            }

            is PurchaseReturnContract.Event.StoreChanged -> {
                event.store?.let {
                    state.value.currentOrderInput.items.forEach { item ->
                        item.product?.let { product ->
                            observeStockForItem(item.tempEditorId, product.id)
                        }
                    }
                }
                setState(event)
            }

            is PurchaseReturnContract.Event.OrderSelected -> {
                setState(event)
                state.value.currentOrderInput.items.forEach { item ->
                    item.product?.let { product ->
                        observeStockForItem(item.tempEditorId, product.id)
                    }
                }
            }

            is PurchaseReturnContract.Event.LoadInitialInvoice -> {
                loadInitialInvoice(event.id)
            }

            else -> setState(event)
        }
    }

    init {
        loadInitialData()
    }

    private fun loadInitialInvoice(id: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            id?.let {
                invoiceRepository.getInvoiceById(it).onSuccess { invoice ->
                    setState(PurchaseReturnContract.Event.OrderSelected(invoice))
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: throw Exception()
            setState(PurchaseReturnContract.Event.UserLoaded(user))
            combine(
                partnerRepository.getSuppliers(""),
                productRepository.getProducts(""),
                storeRepository.getStoresForUser(user)
            ) { clients, products, stores ->
                PurchaseReturnContract.DropdownData(clients, products, stores)
            }.collect {
                handleEvent(PurchaseReturnContract.Event.DropdownDataLoaded(it))
                handleEvent(PurchaseReturnContract.Event.PartnerSelected(it.partners.firstOrNull()))
                handleEvent(PurchaseReturnContract.Event.StoreChanged(it.stores.firstOrNull()))
            }
        }
    }

    private fun saveOrder() {
        viewModelScope.launch {
            val currentState = state.value
            if (!currentState.canSave || currentState.currentOrderInput.selectedPartner == null || currentState.currentOrderInput.selectedStore == null) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_some_field_are_required)))
                return@launch
            }
            setState(PurchaseReturnContract.Event.LoadingStarted)

            val orderItems = currentState.currentOrderInput.items.mapNotNull {
                val quantity = it.mainUnitQuantity.toDoubleOrNull()
                val price = it.mainUnitPrice.toDoubleOrNull()
                if (it.product != null && quantity != null && quantity > 0 && price != null) {
                    InvoiceItem(
                        id = "",
                        product = it.product,
                        quantity = quantity,
                        unitPrice = price,
                        isSynced = false
                    )
                } else null
            }

            if (orderItems.size != currentState.currentOrderInput.items.size) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.one_or_more_order_items_are_invalid)))
                setState(PurchaseReturnContract.Event.LoadingFinished)
                return@launch
            }

            currentState.currentOrderInput.items.forEach { item ->
                val validStock =
                    if (item.isSelectedUnitIsMax)
                        item.currentStock - (item.mainUnitQuantity.toDoubleOrNull() ?: 0.0) > 0
                    else item.currentStock - (item.subUnitQuantity.toDoubleOrNull() ?: 0.0) >= 0

                if (!validStock) {
                    snackbarController.sendEvent(
                        SnackbarEvent(
                            StringResource.FromResource(
                                Res.string.invalid_item_with_args,
                                item.product?.name?.enName ?: ""
                            )
                        )
                    )
                    return@launch
                }
            }

            val orderToSave = Invoice(
                partner = currentState.currentOrderInput.selectedPartner,
                employee = currentState.currentUser!!,
                paidAmount = currentState.currentOrderInput.amountPaid.toDoubleOrNull() ?: 0.0,
                createdAt = currentState.selectedOrder?.createdAt ?: Clock.now(),
                items = orderItems,
                isSynced = false,
                totalAmount = currentState.currentOrderInput.totalAmount,
                updatedAt = currentState.selectedOrder?.updatedAt ?: Clock.now(),
                id = currentState.selectedOrder?.id ?: "",
                orderDate = currentState.currentOrderInput.date,
                invoiceType = InvoiceType.PURCHASE_RETURN,
                paymentMethod = currentState.currentOrderInput.paymentType,
                store = currentState.currentOrderInput.selectedStore
            )

            val result = if (currentState.isEditing) {
                invoiceRepository.updateOrder(orderToSave)
            } else {
                invoiceRepository.addSalesOrder(orderToSave)
            }

            result.onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.order_saved)))
                setState(PurchaseReturnContract.Event.SaveSucceeded)
            }.onFailure {
                it.printStackTrace()
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.something_went_wrong)))
                setState(PurchaseReturnContract.Event.LoadingFinished)
            }
        }
    }

    private fun deleteOrder() {
        setState(PurchaseReturnContract.Event.LoadingStarted)
        viewModelScope.launch {
            state.value.selectedOrder?.id?.let {
                invoiceRepository.deleteSalesOrder(it).onSuccess {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.order_deleted)))
                    setState(PurchaseReturnContract.Event.DeleteSucceeded)
                }.onFailure {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_deleting_order)))
                    setState(PurchaseReturnContract.Event.LoadingFinished)
                }
            } ?: snackbarController.sendEvent(
                SnackbarEvent(StringResource.FromResource(Res.string.error_no_order_selected))
            )
        }
    }

    private fun observeStockForItem(tempId: String, productId: String) {
        stockObservationJobs[tempId]?.cancel()
        viewModelScope.launch {
            val storeId = state.value.currentOrderInput.selectedStore?.id ?: return@launch
            stockObservationJobs[tempId] =
                stockRepository.getStockQuantity(storeId, productId).catch {
                    snackbarController.sendEvent(
                        SnackbarEvent(StringResource.FromResource(Res.string.error_fetching_stock))
                    )
                }.onEach { stock ->
                    setState(PurchaseReturnContract.Event.ItemStockChanged(tempId, stock))
                }.launchIn(viewModelScope)
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            navigationController.navigateBack()
        }
    }
}

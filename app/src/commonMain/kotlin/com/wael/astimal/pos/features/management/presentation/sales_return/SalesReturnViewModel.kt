package com.wael.astimal.pos.features.management.presentation.sales_return

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.HtmlReportGenerator
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceType
import com.wael.astimal.pos.features.management.data.local.entity.PaymentMethod
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
import pos.app.generated.resources.one_or_more_order_items_are_invalid
import pos.app.generated.resources.order_deleted
import pos.app.generated.resources.order_saved
import pos.app.generated.resources.something_went_wrong

class SalesReturnViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val storeRepository: StoreRepository,
    private val partnerRepository: BusinessPartnerRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val stockRepository: StockRepository,
    private val snackbarController: SnackbarController,
    private val navigationController: NavigationController,
    private val htmlReportGenerator: HtmlReportGenerator,
) : BaseViewModel<SalesReturnContract.State, SalesReturnContract.Event, Nothing>(
    reducer = SalesReturnReducer(),
    initialState = SalesReturnContract.State(
        currentOrderInput = SalesReturnContract.EditableOrder(date = Clock.now())
    )
) {
    private val stockObservationJobs = mutableMapOf<String, Job>()

    val filteredOrdersState: StateFlow<List<Invoice>> = combine(
        state, invoiceRepository.getInvoices().map { list ->
            list.filter { it.invoiceType == InvoiceType.SALES_RETURN }
        }
    ) { state, allOrders ->
        if (state.orders != allOrders) {
            setState(SalesReturnContract.Event.OrdersLoaded(allOrders))
        }
        if (state.searchQuery.isBlank()) {
            allOrders
        } else {
            allOrders.filter { it.matchesQuery(state.searchQuery) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    override fun handleEvent(event: SalesReturnContract.Event) {
        when (event) {
            is SalesReturnContract.Event.SaveClicked -> {
                stockObservationJobs.values.forEach { it.cancel() }
                stockObservationJobs.clear()
                saveOrder()
            }

            is SalesReturnContract.Event.DeleteClicked -> {
                stockObservationJobs.values.forEach { it.cancel() }
                stockObservationJobs.clear()
                deleteOrder()
            }

            is SalesReturnContract.Event.BackClicked -> {
                stockObservationJobs.values.forEach { it.cancel() }
                stockObservationJobs.clear()
                navigateBack()
            }

            is SalesReturnContract.Event.RemoveItem -> {
                stockObservationJobs[event.editorId]?.cancel()
                stockObservationJobs.remove(event.editorId)
                setState(event)
            }

            is SalesReturnContract.Event.PartnerSelected -> {
                viewModelScope.launch {
                    event.partner?.let {
                        val balance = partnerRepository.getPartnerBalance(it).getOrDefault(0.0)
                        setState(SalesReturnContract.Event.PartnerBalanceChanged(balance))
                    }
                    setState(event)
                }
            }

            is SalesReturnContract.Event.ItemProductChanged -> {
                event.product?.let { observeStockForItem(event.editorId, it.id) }
                setState(event)
            }

            is SalesReturnContract.Event.StoreChanged -> {
                event.store?.let {
                    state.value.currentOrderInput.items.forEach { item ->
                        item.product?.let { product ->
                            observeStockForItem(item.tempEditorId, product.id)
                        }
                    }
                }
                setState(event)
            }

            is SalesReturnContract.Event.OrderSelected -> {
                setState(event)
                state.value.currentOrderInput.items.forEach { item ->
                    item.product?.let { product ->
                        observeStockForItem(item.tempEditorId, product.id)
                    }
                }
            }

            is SalesReturnContract.Event.LoadInitialInvoice -> {
                loadInitialInvoice(event.id)
            }

            is SalesReturnContract.Event.GeneratePdf -> {
                viewModelScope.launch(Dispatchers.IO) {
                    generatePdf(event.invoice)
                }
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
                    setState(SalesReturnContract.Event.OrderSelected(invoice))
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser() ?: throw Exception()
            setState(SalesReturnContract.Event.UserLoaded(user))
            combine(
                partnerRepository.getClients(""),
                productRepository.getProducts(""),
                storeRepository.getStoresForUser(user)
            ) { clients, products, stores ->
                SalesReturnContract.DropdownData(clients, products, stores)
            }.collect {
                handleEvent(SalesReturnContract.Event.DropdownDataLoaded(it))
                handleEvent(SalesReturnContract.Event.PartnerSelected(it.partners.firstOrNull()))
                handleEvent(SalesReturnContract.Event.StoreChanged(it.stores.firstOrNull()))
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
            setState(SalesReturnContract.Event.LoadingStarted)

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
                setState(SalesReturnContract.Event.LoadingFinished)
                return@launch
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
                invoiceType = InvoiceType.SALES_RETURN,
                paymentMethod = currentState.currentOrderInput.paymentType ?: PaymentMethod.CASH,
                store = currentState.currentOrderInput.selectedStore
            )

            val result = if (currentState.isEditing) {
                invoiceRepository.updateOrder(orderToSave)
            } else {
                invoiceRepository.addSalesOrder(orderToSave)
            }

            result.onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.order_saved)))
                setState(SalesReturnContract.Event.SaveSucceeded)
            }.onFailure {
                it.printStackTrace()
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.something_went_wrong)))
                setState(SalesReturnContract.Event.LoadingFinished)
            }
        }
    }

    private fun deleteOrder() {
        setState(SalesReturnContract.Event.LoadingStarted)
        viewModelScope.launch {
            state.value.selectedOrder?.id?.let {
                invoiceRepository.deleteSalesOrder(it).onSuccess {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.order_deleted)))
                    setState(SalesReturnContract.Event.DeleteSucceeded)
                }.onFailure {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_deleting_order)))
                    setState(SalesReturnContract.Event.LoadingFinished)
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
                    setState(SalesReturnContract.Event.ItemStockChanged(tempId, stock))
                }.launchIn(viewModelScope)
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            navigationController.navigateBack()
        }
    }

    private suspend fun generatePdf(
        invoice: Invoice
    ) {
        val html = htmlReportGenerator.createInvoiceHtml(invoice = invoice)

        setState(SalesReturnContract.Event.PdfGenerationSuccess(html))
    }
}

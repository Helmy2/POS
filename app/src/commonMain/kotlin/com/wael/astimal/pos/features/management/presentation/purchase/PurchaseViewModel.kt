package com.wael.astimal.pos.features.management.presentation.purchase

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
import com.wael.astimal.pos.features.management.domain.entity.PurchaseOrder
import com.wael.astimal.pos.features.management.domain.entity.PurchaseOrderItem
import com.wael.astimal.pos.features.management.domain.entity.matchesQuery
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.management.domain.repository.PurchaseRepository
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
import pos.app.generated.resources.Res
import pos.app.generated.resources.error_fetching_stock
import pos.app.generated.resources.error_some_field_are_required
import pos.app.generated.resources.failed_to_delete_purchase
import pos.app.generated.resources.failed_to_save_purchase
import pos.app.generated.resources.one_or_more_order_items_are_invalid
import pos.app.generated.resources.purchase_deleted_successfully
import pos.app.generated.resources.purchase_saved_successfully

class PurchaseViewModel(
    private val purchaseRepository: PurchaseRepository,
    private val partnerRepository: BusinessPartnerRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val stockRepository: StockRepository,
    private val snackbarController: SnackbarController,
    private val navigationController: NavigationController,
) : BaseViewModel<PurchaseContract.State, PurchaseContract.Event, Nothing>(
    reducer = PurchaseReducer(),
    initialState = PurchaseContract.State(
        currentPurchaseInput = PurchaseContract.EditablePurchase(date = Clock.now())
    )
) {
    private val stockObservationJobs = mutableMapOf<String, Job>()

    val filteredPurchasesState: StateFlow<List<PurchaseOrder>> =
        combine(
            state,
            purchaseRepository.getPurchases()
        ) { state, allPurchases ->
            if (state.purchases != allPurchases) {
                setState(PurchaseContract.Event.PurchasesLoaded(allPurchases))
            }
            if (state.searchQuery.isBlank()) {
                allPurchases
            } else {
                allPurchases.filter { it.matchesQuery(state.searchQuery) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    override fun handleEvent(event: PurchaseContract.Event) {
        when (event) {
            is PurchaseContract.Event.SaveClicked -> {
                stockObservationJobs.values.forEach { it.cancel() }
                stockObservationJobs.clear()
                savePurchase()
            }

            is PurchaseContract.Event.DeleteClicked -> {
                stockObservationJobs.values.forEach { it.cancel() }
                stockObservationJobs.clear()
                deletePurchase()
            }

            is PurchaseContract.Event.BackClicked -> {
                stockObservationJobs.values.forEach { it.cancel() }
                stockObservationJobs.clear()
                navigateBack()
            }

            is PurchaseContract.Event.RemoveItem -> {
                stockObservationJobs[event.editorId]?.cancel()
                stockObservationJobs.remove(event.editorId)
                setState(event)
            }

            is PurchaseContract.Event.ItemProductChanged -> {
                event.product?.let {
                    observeStockForItem(event.editorId, it.id.local)
                }
                setState(event)
            }

            is PurchaseContract.Event.SupplierSelected -> {
                viewModelScope.launch {
                    event.supplier?.let {
                        val balance = partnerRepository.getPartnerBalance(it).getOrDefault(0.0)
                        setState(PurchaseContract.Event.PartnerBalanceChanged(balance))
                    }
                    setState(event)
                }
            }
            else -> setState(event)
        }
    }

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            setState(PurchaseContract.Event.UserLoaded(userRepository.getCurrentUser()))
        }
        viewModelScope.launch {
            combine(
                partnerRepository.getSuppliers(""),
                productRepository.getProducts(""),
                userRepository.getEmployeesFlow()
            ) { suppliers, products, employees ->
                PurchaseContract.DropdownData(suppliers, products, employees)
            }.collect {
                setState(PurchaseContract.Event.DropdownDataLoaded(it))
            }
        }
    }

    private fun savePurchase() {
        viewModelScope.launch {
            val currentState = state.value
            if (!currentState.canSave || currentState.selectedSupplier == null || currentState.currentPurchaseInput.selectedEmployee == null) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_some_field_are_required)))
                return@launch
            }
            setState(PurchaseContract.Event.LoadingStarted)

            val purchaseItems = currentState.currentPurchaseInput.items.mapNotNull {
                val quantity = it.maxUnitQuantity.toDoubleOrNull()
                val price = it.maxUnitPrice.toDoubleOrNull()
                if (it.product != null && quantity != null && quantity > 0 && price != null) {
                    PurchaseOrderItem(
                        id = Id.new,
                        product = it.product,
                        quantity = quantity,
                        purchasePrice = price,
                        itemTotalPrice = it.lineTotal
                    )
                } else null
            }

            if (purchaseItems.size != currentState.currentPurchaseInput.items.size) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.one_or_more_order_items_are_invalid)))
                setState(PurchaseContract.Event.LoadingFinished)
                return@launch
            }

            val purchaseToSave = PurchaseOrder(
                supplier = currentState.selectedSupplier,
                user = currentState.currentPurchaseInput.selectedEmployee,
                paymentType = currentState.currentPurchaseInput.paymentType,
                amountPaid = currentState.currentPurchaseInput.amountPaid.toDoubleOrNull() ?: 0.0,
                data = currentState.currentPurchaseInput.date,
                items = purchaseItems,
                isSynced = false,
                totalAmount = currentState.currentPurchaseInput.totalAmount,
                createdAt = currentState.selectedPurchase?.createdAt ?: Clock.now(),
                updatedAt = currentState.selectedPurchase?.updatedAt ?: Clock.now(),
                id = currentState.selectedPurchase?.id ?: Id.new,
                invoiceNumber = currentState.selectedPurchase?.invoiceNumber ?: "",
                amountRemaining = currentState.currentPurchaseInput.amountRemaining
            )

            val result = if (currentState.isEditing) {
                purchaseRepository.updatePurchase(purchaseToSave)
            } else {
                purchaseRepository.addPurchase(purchaseToSave)
            }

            result.onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.purchase_saved_successfully)))
                setState(PurchaseContract.Event.SaveSucceeded)
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_save_purchase)))
                setState(PurchaseContract.Event.LoadingFinished)
            }
        }
    }

    private fun deletePurchase() {
        val purchaseToDelete = state.value.selectedPurchase ?: return
        setState(PurchaseContract.Event.LoadingStarted)
        viewModelScope.launch {
            purchaseRepository.deletePurchase(purchaseToDelete.id.local).onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.purchase_deleted_successfully)))
                setState(PurchaseContract.Event.DeleteSucceeded)
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_delete_purchase)))
                setState(PurchaseContract.Event.LoadingFinished)
            }
        }
    }

    private fun observeStockForItem(tempId: String, productId: Long) {
        stockObservationJobs[tempId]?.cancel()
        viewModelScope.launch {
            val employeeId = state.value.currentPurchaseInput.selectedEmployee?.id ?: return@launch
            val storeId =
                userRepository.getStoreIdForEmployee(employeeId).getOrNull() ?: return@launch
            stockObservationJobs[tempId] =
                stockRepository.getStockQuantityFlow(storeId, productId)
                    .catch {
                        snackbarController.sendEvent(
                            SnackbarEvent(StringResource.FromResource(Res.string.error_fetching_stock))
                        )
                    }
                    .onEach { stock ->
                        setState(PurchaseContract.Event.ItemStockChanged(tempId, stock))
                    }.launchIn(viewModelScope)
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            navigationController.navigateBack()
        }
    }
}
package com.wael.astimal.pos.features.management.presentation.purchase

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.management.domain.entity.PurchaseOrder
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class PurchaseViewModel(
//    private val purchaseRepository: PurchaseRepository,
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
        MutableStateFlow(emptyList())

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

    }

    private fun deletePurchase() {

    }

    private fun observeStockForItem(tempId: String, productId: Long) {
//        stockObservationJobs[tempId]?.cancel()
//        viewModelScope.launch {
//            val employeeId = state.value.currentPurchaseInput.selectedEmployee?.id ?: return@launch
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
//                        setState(PurchaseContract.Event.ItemStockChanged(tempId, stock))
//                    }.launchIn(viewModelScope)
//        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            navigationController.navigateBack()
        }
    }
}
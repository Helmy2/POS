package com.wael.astimal.pos.features.management.presentation.purchase_return

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
import com.wael.astimal.pos.features.management.domain.entity.PurchaseReturn
import com.wael.astimal.pos.features.management.domain.entity.PurchaseReturnItem
import com.wael.astimal.pos.features.management.domain.entity.matchesQuery
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.management.domain.repository.PurchaseReturnRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PurchaseReturnViewModel(
    private val purchaseReturnRepository: PurchaseReturnRepository,
    private val partnerRepository: BusinessPartnerRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val snackbarController: SnackbarController,
    private val navigationController: NavigationController,
) : BaseViewModel<PurchaseReturnContract.State, PurchaseReturnContract.Event, Nothing>(
    reducer = PurchaseReturnReducer(),
    initialState = PurchaseReturnContract.State(
        currentReturnInput = PurchaseReturnContract.EditableReturn(date = Clock.now())
    )
) {

    val filteredReturnsState: StateFlow<List<PurchaseReturn>> =
        combine(
            state,
            purchaseReturnRepository.getPurchaseReturns()
        ) { state, allReturns ->
            if (state.returns != allReturns) {
                setState(PurchaseReturnContract.Event.ReturnsLoaded(allReturns))
            }
            if (state.searchQuery.isBlank()) {
                allReturns
            } else {
                allReturns.filter { it.matchesQuery(state.searchQuery) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    override fun handleEvent(event: PurchaseReturnContract.Event) {
        when (event) {
            is PurchaseReturnContract.Event.SaveClicked -> saveReturn()
            is PurchaseReturnContract.Event.DeleteClicked -> deleteReturn()
            is PurchaseReturnContract.Event.BackClicked -> navigateBack()
            is PurchaseReturnContract.Event.SupplierSelected -> {
                viewModelScope.launch {
                    event.supplier?.let {
                        val balance = partnerRepository.getPartnerBalance(it).getOrDefault(0.0)
                        setState(PurchaseReturnContract.Event.PartnerBalanceChanged(balance))
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
            setState(PurchaseReturnContract.Event.UserLoaded(userRepository.getCurrentUser()))
        }
        viewModelScope.launch {
            combine(
                partnerRepository.getSuppliers(""),
                productRepository.getProducts(""),
                userRepository.getEmployeesFlow()
            ) { suppliers, products, employees ->
                PurchaseReturnContract.DropdownData(suppliers, products, employees)
            }.collect {
                setState(PurchaseReturnContract.Event.DropdownDataLoaded(it))
            }
        }
    }

    private fun saveReturn() {
        viewModelScope.launch {
            val currentState = state.value
            if (!currentState.canSave || currentState.selectedSupplier == null || currentState.currentReturnInput.selectedEmployee == null) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.error_some_field_are_required)))
                return@launch
            }
            setState(PurchaseReturnContract.Event.LoadingStarted)

            val returnItems = currentState.currentReturnInput.items.mapNotNull {
                val quantity = it.maxUnitQuantity.toDoubleOrNull()
                val price = it.maxUnitPrice.toDoubleOrNull()
                if (it.product != null && quantity != null && quantity > 0 && price != null) {
                    PurchaseReturnItem(
                        id = Id.new,
                        product = it.product,
                        quantity = quantity,
                        purchasePrice = price,
                        itemTotalPrice = it.lineTotal,
                    )
                } else null
            }

            if (returnItems.size != currentState.currentReturnInput.items.size) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.one_or_more_order_items_are_invalid)))
                setState(PurchaseReturnContract.Event.LoadingFinished)
                return@launch
            }

            val returnToSave = PurchaseReturn(
                supplier = currentState.selectedSupplier,
                employee = currentState.currentReturnInput.selectedEmployee,
                paymentType = currentState.currentReturnInput.paymentType,
                amountPaid = currentState.currentReturnInput.amountPaid.toDoubleOrNull() ?: 0.0,
                data = currentState.currentReturnInput.date,
                items = returnItems,
                isSynced = false,
                totalAmount = currentState.currentReturnInput.totalAmount,
                createdAt = currentState.selectedReturn?.createdAt ?: Clock.now(),
                updatedAt = currentState.selectedReturn?.updatedAt ?: Clock.now(),
                id = currentState.selectedReturn?.id ?: Id.new,
                invoiceNumber = currentState.selectedReturn?.invoiceNumber ?: "",
                amountRemaining = currentState.currentReturnInput.amountRemaining
            )

            val result = if (currentState.isEditing) {
                purchaseReturnRepository.updatePurchaseReturn(returnToSave)
            } else {
                purchaseReturnRepository.addPurchaseReturn(returnToSave)
            }

            result.onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.purchase_return_saved)))
                setState(PurchaseReturnContract.Event.SaveSucceeded)
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.something_went_wrong)))
                setState(PurchaseReturnContract.Event.LoadingFinished)
            }
        }
    }

    private fun deleteReturn() {
        val returnToDelete = state.value.selectedReturn ?: return
        setState(PurchaseReturnContract.Event.LoadingStarted)
        viewModelScope.launch {
            purchaseReturnRepository.deletePurchaseReturn(returnToDelete.id.local).onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.purchase_return_deleted)))
                setState(PurchaseReturnContract.Event.DeleteSucceeded)
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(R.string.error_deleting_purchase_return)))
                setState(PurchaseReturnContract.Event.LoadingFinished)
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            navigationController.navigateBack()
        }
    }
}

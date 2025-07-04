package com.wael.astimal.pos.features.management.presentation.sales_return

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.management.domain.entity.SalesReturn
import com.wael.astimal.pos.features.management.domain.entity.SalesReturnItem
import com.wael.astimal.pos.features.management.domain.entity.matchesQuery
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.management.domain.repository.SalesReturnRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pos.app.generated.resources.Res
import pos.app.generated.resources.error_deleting_return
import pos.app.generated.resources.error_some_field_are_required
import pos.app.generated.resources.one_or_more_order_items_are_invalid
import pos.app.generated.resources.return_deleted
import pos.app.generated.resources.return_saved
import pos.app.generated.resources.something_went_wrong

class SalesReturnViewModel(
    private val salesReturnRepository: SalesReturnRepository,
    private val partnerRepository: BusinessPartnerRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val snackbarController: SnackbarController,
    private val navigationController: NavigationController,
) : BaseViewModel<SalesReturnContract.State, SalesReturnContract.Event, Nothing>(
    reducer = SalesReturnReducer(),
    initialState = SalesReturnContract.State(
        currentReturnInput = SalesReturnContract.EditableReturn(createdAt = Clock.now())
    )
) {
    val filteredReturnsState: StateFlow<List<SalesReturn>> =
        combine(
            state,
            salesReturnRepository.getReturns("")
        ) { state, allReturns ->
            if (state.returns != allReturns) {
                setState(SalesReturnContract.Event.ReturnsLoaded(allReturns))
            }
            if (state.searchQuery.isBlank()) {
                allReturns
            } else {
                allReturns.filter { it.matchesQuery(state.searchQuery) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    override fun handleEvent(event: SalesReturnContract.Event) {
        when (event) {
            is SalesReturnContract.Event.SaveClicked -> saveReturn()
            is SalesReturnContract.Event.DeleteClicked -> deleteReturn()
            is SalesReturnContract.Event.BackClicked -> navigateBack()
            is SalesReturnContract.Event.ClientSelected -> {
                viewModelScope.launch {
                    event.client?.let {
                        val balance = partnerRepository.getPartnerBalance(it).getOrDefault(0.0)
                        setState(SalesReturnContract.Event.PartnerBalanceChanged(balance))
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
            setState(SalesReturnContract.Event.UserLoaded(userRepository.getCurrentUser()))
        }
        viewModelScope.launch {
            combine(
                partnerRepository.getClients(""),
                productRepository.getProducts(""),
                userRepository.getEmployeesFlow()
            ) { clients, products, employees ->
                SalesReturnContract.DropdownData(clients, products, employees)
            }.collect {
                setState(SalesReturnContract.Event.DropdownDataLoaded(it))
            }
        }
    }

    private fun saveReturn() {
        viewModelScope.launch {
            val currentState = state.value
            if (!currentState.canSave || currentState.selectedClient == null || currentState.currentReturnInput.selectedEmployee == null) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_some_field_are_required)))
                return@launch
            }
            setState(SalesReturnContract.Event.LoadingStarted)

            val returnItems = currentState.currentReturnInput.items.mapNotNull {
                val quantity = it.maxUnitQuantity.toDoubleOrNull()
                val price = it.maxUnitPrice.toDoubleOrNull()
                if (it.product != null && quantity != null && quantity > 0 && price != null) {
                    SalesReturnItem(
                        id = Id.new,
                        product = it.product,
                        quantity = quantity,
                        priceAtReturn = price,
                        itemTotalValue = it.lineTotal,
                    )
                } else null
            }

            if (returnItems.size != currentState.currentReturnInput.items.size) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.one_or_more_order_items_are_invalid)))
                setState(SalesReturnContract.Event.LoadingFinished)
                return@launch
            }

            val returnToSave = SalesReturn(
                client = currentState.selectedClient,
                employee = currentState.currentReturnInput.selectedEmployee,
                paymentType = currentState.currentReturnInput.paymentType,
                amountPaid = currentState.currentReturnInput.amountPaid.toDoubleOrNull() ?: 0.0,
                createdAt = currentState.currentReturnInput.createdAt,
                items = returnItems,
                totalAmount = currentState.currentReturnInput.totalAmount,
                id = currentState.selectedReturn?.id ?: Id.new,
                invoiceNumber = currentState.selectedReturn?.invoiceNumber ?: "",
                amountRemaining = currentState.currentReturnInput.amountRemaining
            )

            val result = if (currentState.isEditing) {
                salesReturnRepository.updateReturn(returnToSave)
            } else {
                salesReturnRepository.addReturn(returnToSave)
            }

            result.onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.return_saved)))
                setState(SalesReturnContract.Event.SaveSucceeded)
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.something_went_wrong)))
                setState(SalesReturnContract.Event.LoadingFinished)
            }
        }
    }

    private fun deleteReturn() {
        val returnToDelete = state.value.selectedReturn ?: return
        setState(SalesReturnContract.Event.LoadingStarted)
        viewModelScope.launch {
            salesReturnRepository.deleteReturn(returnToDelete.id.local).onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.return_deleted)))
                setState(SalesReturnContract.Event.DeleteSucceeded)
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_deleting_return)))
                setState(SalesReturnContract.Event.LoadingFinished)
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            navigationController.navigateBack()
        }
    }
}

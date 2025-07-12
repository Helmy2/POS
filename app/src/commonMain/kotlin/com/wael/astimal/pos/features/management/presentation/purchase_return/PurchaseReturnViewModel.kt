package com.wael.astimal.pos.features.management.presentation.purchase_return

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.management.domain.entity.PurchaseReturn
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class PurchaseReturnViewModel(
//    private val purchaseReturnRepository: PurchaseReturnRepository,
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
        MutableStateFlow(emptyList())

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

    }

    private fun deleteReturn() {

    }

    private fun navigateBack() {
        viewModelScope.launch {
            navigationController.navigateBack()
        }
    }
}

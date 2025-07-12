package com.wael.astimal.pos.features.management.presentation.sales_return

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.management.domain.entity.SalesReturn
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SalesReturnViewModel(
//    private val salesReturnRepository: SalesReturnRepository,
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
        MutableStateFlow(emptyList())

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

    }

    private fun deleteReturn() {

    }

    private fun navigateBack() {
        viewModelScope.launch {
            navigationController.navigateBack()
        }
    }
}

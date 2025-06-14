package com.wael.astimal.pos.features.management.presentation.purchase_return

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.management.data.entity.PurchaseReturnEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseReturnProductEntity
import com.wael.astimal.pos.features.management.domain.entity.EditableItem
import com.wael.astimal.pos.features.management.domain.entity.EditableItemList
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.PurchaseReturn
import com.wael.astimal.pos.features.management.domain.repository.PurchaseReturnRepository
import com.wael.astimal.pos.features.management.domain.repository.SupplierRepository
import com.wael.astimal.pos.features.user.domain.entity.User
import com.wael.astimal.pos.features.user.domain.entity.UserType
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PurchaseReturnViewModel(
    private val purchaseReturnRepository: PurchaseReturnRepository,
    private val supplierRepository: SupplierRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(PurchaseReturnState())
    val state: StateFlow<PurchaseReturnState> = _state.asStateFlow()
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            sessionManager.getCurrentUser().collect { updateCurrentUser(it) }
        }
        onEvent(PurchaseReturnEvent.SearchReturns(""))
        loadDropdownData()
    }

    private fun loadDropdownData() {
        viewModelScope.launch {
            supplierRepository.getSuppliers()
                .collect { result -> _state.update { it.copy(availableSuppliers = result) } }
        }
        viewModelScope.launch {
            productRepository.getProducts()
                .collect { result -> _state.update { it.copy(availableProducts = result) } }
        }
        viewModelScope.launch {
            userRepository.getEmployeesFlow()
                .collect { result -> _state.update { it.copy(availableEmployees = result) } }
        }
    }

    private fun updateCurrentUser(user: User?) {
        _state.update {
            when {
                user == null -> it
                user.userType == UserType.ADMIN -> it.copy(currentUser = user)
                else -> it.copy(
                    currentUser = user,
                    currentReturnInput = it.currentReturnInput.copy(selectedEmployeeId = user.id)
                )
            }
        }
    }

    fun onEvent(event: PurchaseReturnEvent) {
        when (event) {
            is PurchaseReturnEvent.SearchReturns -> searchReturns(event.query)
            is PurchaseReturnEvent.SelectReturnToView -> updateSelectedReturn(event.purchaseReturn)
            is PurchaseReturnEvent.SelectSupplier -> _state.update { it.copy(selectedSupplier = event.supplier) }
            is PurchaseReturnEvent.SelectEmployee -> updateReturnInput { it.copy(selectedEmployeeId = event.employeeId) }
            is PurchaseReturnEvent.UpdatePaymentType -> updateReturnInput {
                it.copy(paymentType = event.type ?: PaymentType.CASH)
            }

            is PurchaseReturnEvent.UpdateAmountPaid -> updateReturnInput { it.copy(amountPaid = event.amount) }
            is PurchaseReturnEvent.AddItemToReturn -> updateReturnInput { it.copy(items = it.items + EditableItem()) }
            is PurchaseReturnEvent.RemoveItemFromReturn -> updateReturnInput { it.copy(items = it.items.filterNot { item -> item.tempEditorId == event.tempEditorId }) }
            is PurchaseReturnEvent.UpdateItemProduct -> updateReturnItem(event.tempEditorId) {
                val conversionFactor = event.product?.subUnitsPerMainUnit ?: 1.0
                it.copy(
                    product = event.product,
                    minUnitPrice = (event.product?.averagePrice?.div(conversionFactor)).toString(),
                    maxUnitPrice = event.product?.averagePrice.toString(),
                    minUnitQuantity = conversionFactor.toString(),
                    maxUnitQuantity = "1.0",
                )
            }

            is PurchaseReturnEvent.UpdateItemUnit -> updateReturnItem(event.tempEditorId) {
                it.copy(isSelectedUnitIsMax = event.isMaxUnitSelected)
            }

            is PurchaseReturnEvent.SaveReturn -> saveReturn()
            is PurchaseReturnEvent.ClearSnackbar -> _state.update { it.copy(snackbarMessage = null) }
            is PurchaseReturnEvent.UpdateIsQueryActive -> _state.update { it.copy(isQueryActive = event.isActive) }
            is PurchaseReturnEvent.UpdateQuery -> _state.update { it.copy(query = event.query) }
            is PurchaseReturnEvent.DeleteReturn -> deleteReturn()
            PurchaseReturnEvent.OpenNewReturnForm -> clearState()
            PurchaseReturnEvent.ClearError -> _state.update { it.copy(error = null) }
            is PurchaseReturnEvent.UpdateReturnDate -> updateReturnInput {
                it.copy(date = event.date ?: System.currentTimeMillis())
            }

            is PurchaseReturnEvent.UpdateItemMaxUnitPrice -> updateReturnItem(event.tempEditorId) {
                val conversionFactor = it.product?.subUnitsPerMainUnit ?: 1.0
                it.copy(
                    maxUnitPrice = event.price,
                    minUnitPrice = (event.price.toDoubleOrNull()?.div(conversionFactor))?.toString()
                        ?: "0.0"
                )
            }

            is PurchaseReturnEvent.UpdateItemMinUnitPrice -> updateReturnItem(event.tempEditorId) {
                val conversionFactor = it.product?.subUnitsPerMainUnit ?: 1.0
                it.copy(
                    minUnitPrice = event.price,
                    maxUnitPrice = (event.price.toDoubleOrNull()
                        ?.times(conversionFactor))?.toString() ?: "0.0"
                )
            }

            is PurchaseReturnEvent.UpdateItemMaxUnitQuantity -> updateReturnItem(event.tempEditorId) {
                val conversionFactor = it.product?.subUnitsPerMainUnit ?: 1.0
                it.copy(
                    maxUnitQuantity = event.quantity,
                    minUnitQuantity = (event.quantity.toDoubleOrNull()
                        ?.times(conversionFactor))?.toString() ?: "0.0"
                )
            }

            is PurchaseReturnEvent.UpdateItemMinUnitQuantity -> updateReturnItem(event.tempEditorId) {
                val conversionFactor = it.product?.subUnitsPerMainUnit ?: 1.0
                it.copy(
                    minUnitQuantity = event.quantity,
                    maxUnitQuantity = (event.quantity.toDoubleOrNull()
                        ?.div(conversionFactor))?.toString() ?: "0.0"
                )
            }
        }
    }

    private fun deleteReturn() {
        viewModelScope.launch {
            _state.value.selectedReturn?.localId?.let {
                purchaseReturnRepository.deletePurchaseReturn(it).fold(
                    onSuccess = {
                        clearState(snackbarMessage = R.string.purchase_return_deleted)
                    },
                    onFailure = { _state.update { it -> it.copy(error = R.string.error_deleting_purchase_return) } })
            }
        }
    }

    private fun updateSelectedReturn(purchaseReturn: PurchaseReturn?) {
        _state.update {
            it.copy(
                isQueryActive = false,
                selectedReturn = purchaseReturn,
                selectedSupplier = purchaseReturn?.supplier,
                currentReturnInput = if (purchaseReturn == null) EditableItemList(
                    selectedEmployeeId = it.currentUser?.id,
                ) else EditableItemList(
                    selectedEmployeeId = purchaseReturn.employee?.id,
                    paymentType = purchaseReturn.paymentType,
                    date = purchaseReturn.data,
                    items = purchaseReturn.items.map { item ->
                        val conversionFactor = item.product?.subUnitsPerMainUnit ?: 1.0
                        EditableItem(
                            tempEditorId = item.localId.toString(),
                            product = item.product,
                            isSelectedUnitIsMax = item.productUnit?.localId == item.product?.maximumProductUnit?.localId,
                            maxUnitPrice = item.purchasePrice.toString(),
                            minUnitPrice = (item.purchasePrice / conversionFactor).toString(),
                            maxUnitQuantity = item.quantity.toString(),
                            minUnitQuantity = (item.quantity * conversionFactor).toString(),
                        )
                    },
                    amountPaid = purchaseReturn.amountPaid.toString(),
                )
            )
        }
    }

    private fun updateReturnInput(action: (EditableItemList) -> EditableItemList) {
        _state.update { it.copy(currentReturnInput = action(it.currentReturnInput)) }
    }

    private fun updateReturnItem(tempId: String, action: (EditableItem) -> EditableItem) {
        val currentItems = _state.value.currentReturnInput.items.toMutableList()
        val index = currentItems.indexOfFirst { it.tempEditorId == tempId }
        if (index != -1) {
            currentItems[index] = action(currentItems[index])
            updateReturnInput { it.copy(items = currentItems) }
        }
    }

    private fun searchReturns(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, query = query) }
            delay(300)
            purchaseReturnRepository.getPurchaseReturns().catch { _ ->
                _state.update { it.copy(loading = false, error = R.string.error_searching_orders) }
            }.collect { returns ->
                _state.update { it.copy(loading = false, returns = returns) }
            }
        }
    }

    private fun saveReturn() {
        val returnInput = _state.value.currentReturnInput
        val selectedSupplier = _state.value.selectedSupplier
        val loggedInEmployeeId = _state.value.currentUser?.id
        if (loggedInEmployeeId == null) {
            _state.update { it.copy(error = R.string.user_not_identified) }
            return
        }
        if (selectedSupplier == null || returnInput.items.isEmpty()) {
            _state.update { it.copy(error = R.string.supplier_and_at_least_one_item_are_required) }
            return
        }

        val itemEntities = returnInput.items.mapNotNull {
            val quantity = it.maxUnitQuantity.toDoubleOrNull() ?: 0.0
            if (it.product == null || quantity <= 0) return@mapNotNull null
            PurchaseReturnProductEntity(
                productLocalId = it.product.localId,
                unitLocalId = it.product.maximumProductUnit.localId,
                quantity = quantity,
                purchasePrice = it.maxUnitPrice.toDoubleOrNull() ?: 0.0,
                itemTotalPrice = it.lineTotal,
                serverId = null,
                purchaseReturnLocalId = 0L
            )
        }

        if (itemEntities.size != returnInput.items.size) {
            _state.update { it.copy(error = R.string.one_or_more_order_items_are_invalid) }
            return
        }

        val returnEntity = PurchaseReturnEntity(
            localId = _state.value.selectedReturn?.localId ?: 0L,
            serverId = null,
            invoiceNumber = null,
            supplierLocalId = selectedSupplier.id,
            employeeLocalId = returnInput.selectedEmployeeId ?: loggedInEmployeeId,
            amountPaid = returnInput.amountPaid.toDoubleOrNull() ?: 0.0,
            amountRemaining = returnInput.amountRemaining,
            totalAmount = returnInput.totalAmount,
            paymentType = returnInput.paymentType,
            returnDate = returnInput.date
        )

        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val result = if (_state.value.isNew) purchaseReturnRepository.addPurchaseReturn(
                returnEntity, itemEntities
            )
            else purchaseReturnRepository.updatePurchaseReturn(returnEntity, itemEntities)

            result.fold(onSuccess = {
                clearState(snackbarMessage = R.string.purchase_return_saved)
            }, onFailure = {
                _state.update {
                    it.copy(loading = false, error = R.string.something_went_wrong)
                }
            })
        }
    }

    private fun clearState(snackbarMessage: Int? = null) {
        _state.update {
            it.copy(
                selectedReturn = null,
                selectedSupplier = null,
                currentReturnInput = EditableItemList(),
                isQueryActive = false,
                snackbarMessage = snackbarMessage,
            )
        }
        updateCurrentUser(state.value.currentUser)
    }
}

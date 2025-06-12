package com.wael.astimal.pos.features.management.presentaion.purchase_return

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.compoenents.EditableItemList
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.management.data.entity.PurchaseReturnEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseReturnProductEntity
import com.wael.astimal.pos.features.management.domain.entity.EditableItem
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.PurchaseReturn
import com.wael.astimal.pos.features.management.domain.repository.PurchaseReturnRepository
import com.wael.astimal.pos.features.management.domain.repository.SupplierRepository
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

    private val _state = MutableStateFlow(PurchaseReturnScreenState())
    val state: StateFlow<PurchaseReturnScreenState> = _state.asStateFlow()
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            sessionManager.getCurrentUser().collect { user ->
                updateCurrentUser(user)
            }
        }
        onEvent(PurchaseReturnScreenEvent.SearchReturns(""))
        loadDropdownData()
    }

    private fun updateCurrentUser(user: com.wael.astimal.pos.features.user.domain.entity.User?) {
        _state.update {
            when {
                user == null -> it
                user.isAdmin -> it.copy(currentUser = user)
                else -> it.copy(
                    currentUser = user,
                    input = it.input.copy(selectedEmployeeId = user.id)
                )
            }
        }
    }

    private fun loadDropdownData() {
        viewModelScope.launch {
            supplierRepository.getSuppliers("")
                .collect { result -> _state.update { it.copy(availableSuppliers = result) } }
        }
        viewModelScope.launch {
            productRepository.getProducts("")
                .collect { result -> _state.update { it.copy(availableProducts = result) } }
        }
        viewModelScope.launch {
            userRepository.getEmployeesFlow()
                .collect { result -> _state.update { it.copy(availableEmployees = result) } }
        }
    }

    fun onEvent(event: PurchaseReturnScreenEvent) {
        when (event) {
            is PurchaseReturnScreenEvent.SearchReturns -> searchReturns(event.query)
            is PurchaseReturnScreenEvent.SelectReturnToView -> selectReturn(event.purchaseReturn)
            is PurchaseReturnScreenEvent.UpdateIsQueryActive -> _state.update { it.copy(isQueryActive = event.isActive) }
            is PurchaseReturnScreenEvent.UpdateQuery -> _state.update { it.copy(query = event.query) }
            is PurchaseReturnScreenEvent.OpenNewReturnForm -> {
                _state.update { PurchaseReturnScreenState(currentUser = state.value.currentUser) }
                updateCurrentUser(state.value.currentUser)
            }
            is PurchaseReturnScreenEvent.SelectSupplier -> _state.update { it.copy(selectedSupplier = event.supplier) }
            is PurchaseReturnScreenEvent.SelectEmployee -> updateReturnInput { it.copy(selectedEmployeeId = event.id) }
            is PurchaseReturnScreenEvent.UpdatePaymentType -> updateReturnInput { it.copy(paymentType = event.type ?: PaymentType.CASH) }
            is PurchaseReturnScreenEvent.AddItemToReturn -> updateReturnInput { it.copy(items = it.items + EditableItem()) }
            is PurchaseReturnScreenEvent.RemoveItemFromReturn -> updateReturnInput { it.copy(items = it.items.filterNot { item -> item.tempEditorId == event.tempEditorId }) }
            is PurchaseReturnScreenEvent.UpdateItemProduct -> updateReturnItem(event.tempEditorId) {
                it.copy(
                    product = event.product,
                    price = event.product?.averagePrice?.toString() ?: "0.0",
                    selectedProductUnit = event.product?.minimumProductUnit
                )
            }
            is PurchaseReturnScreenEvent.UpdateItemUnit -> updateReturnItem(event.tempEditorId) { it.copy(selectedProductUnit = event.productUnit) }
            is PurchaseReturnScreenEvent.UpdateItemQuantity -> updateReturnItem(event.tempEditorId) { it.copy(quantity = event.quantity) }
            is PurchaseReturnScreenEvent.UpdateItemPrice -> updateReturnItem(event.tempEditorId) { it.copy(price = event.price) }
            is PurchaseReturnScreenEvent.UpdateAmountPaid -> updateReturnInput { it.copy(amountPaid = event.amountPaid) }
            is PurchaseReturnScreenEvent.UpdateTransferDate -> updateReturnInput { it.copy(date = event.date ?: System.currentTimeMillis()) }
            is PurchaseReturnScreenEvent.SaveReturn -> saveReturn()
            is PurchaseReturnScreenEvent.DeleteReturn -> deleteReturn()
            is PurchaseReturnScreenEvent.ClearSnackbar -> _state.update { it.copy(snackbarMessage = null) }
            is PurchaseReturnScreenEvent.ClearError -> _state.update { it.copy(error = null) }
        }
    }

    private fun selectReturn(purchaseReturn: PurchaseReturn?) {
        _state.update {
            it.copy(
                selectedReturn = purchaseReturn,
                isQueryActive = false,
                selectedSupplier = purchaseReturn?.supplier,
                input = if (purchaseReturn == null) EditableItemList(selectedEmployeeId = currentUserId)
                else EditableItemList(
                    selectedEmployeeId = purchaseReturn.employee?.id,
                    paymentType = purchaseReturn.paymentType,
                    date = purchaseReturn.returnDate,
                    items = purchaseReturn.items.map { item ->
                        EditableItem(
                            tempEditorId = item.localId.toString(),
                            product = item.product,
                            selectedProductUnit = item.productUnit,
                            quantity = item.quantity.toString(),
                            price = item.purchasePrice.toString(),
                        )
                    },
                    amountPaid = "0.0"
                )
            )
        }
    }

    private fun updateReturnInput(action: (EditableItemList) -> EditableItemList) {
        _state.update { it.copy(input = action(it.input)) }
    }

    private fun updateReturnItem(
        tempId: String, action: (EditableItem) -> EditableItem
    ) {
        val currentItems = _state.value.input.items.toMutableList()
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
        val returnInput = _state.value.input
        val selectedSupplier = _state.value.selectedSupplier
        val employeeId = _state.value.currentUser?.id
        if (employeeId == null) {
            _state.update { it.copy(error = R.string.user_not_identified) }
            return
        }
        if (selectedSupplier == null || returnInput.items.isEmpty()) {
            _state.update { it.copy(error = R.string.supplier_and_at_least_one_item_are_required) }
            return
        }
        val itemEntities = returnInput.items.mapNotNull {
            val quantity = it.quantity.toDoubleOrNull() ?: 0.0
            if (it.product == null || it.selectedProductUnit == null || quantity <= 0) return@mapNotNull null
            PurchaseReturnProductEntity(
                productLocalId = it.product.localId,
                unitLocalId = it.selectedProductUnit.localId,
                quantity = quantity,
                purchasePrice = it.price.toDoubleOrNull() ?: 0.0,
                itemTotalPrice = quantity * (it.price.toDoubleOrNull() ?: 0.0),
                purchaseReturnLocalId = 1,
                serverId = null,
            )
        }
        if (itemEntities.size != returnInput.items.size) {
            _state.update { it.copy(error = R.string.one_or_more_order_items_are_invalid) }
            return
        }
        val returnEntity = PurchaseReturnEntity(
            localId = _state.value.selectedReturn?.localId ?: 0L,
            supplierLocalId = selectedSupplier.id,
            employeeLocalId = returnInput.selectedEmployeeId ?: employeeId,
            totalPrice = returnInput.totalAmount,
            paymentType = returnInput.paymentType,
            returnDate = System.currentTimeMillis(),
            serverId = null,
            invoiceNumber = null,
        )
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val result = if (_state.value.isNew) purchaseReturnRepository.addPurchaseReturn(
                returnEntity, itemEntities
            )
            else purchaseReturnRepository.updatePurchaseReturn(
                returnEntity, itemEntities
            )

            result.fold(onSuccess = {
                _state.update {
                    it.copy(
                        loading = false,
                        isQueryActive = false,
                        snackbarMessage = R.string.purchase_return_saved,
                        selectedReturn = null,
                        selectedSupplier = null,
                        input = EditableItemList()
                    )
                }
            }, onFailure = { _state.update { it.copy(loading = false, error = R.string.something_went_wrong) } })
        }
    }

    private fun deleteReturn() {
        val purchaseToDelete = _state.value.selectedReturn ?: return
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            purchaseReturnRepository.deletePurchaseReturn(purchaseToDelete.localId).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            loading = false,
                            selectedReturn = null,
                            snackbarMessage = R.string.purchase_return_deleted,
                            input = EditableItemList()
                        )
                    }
                },
                onFailure = {
                    _state.update {
                        it.copy(
                            loading = false, error = R.string.error_deleting_purchase_return
                        )
                    }
                }
            )
            onEvent(PurchaseReturnScreenEvent.SearchReturns(""))
        }
    }

    private val currentUserId: Long?
        get() = _state.value.currentUser?.id
}

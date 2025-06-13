package com.wael.astimal.pos.features.management.presentaion.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.management.domain.entity.EditableItemList
import com.wael.astimal.pos.features.management.data.entity.PurchaseEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseProductEntity
import com.wael.astimal.pos.features.management.domain.entity.EditableItem
import com.wael.astimal.pos.features.management.domain.entity.PurchaseOrder
import com.wael.astimal.pos.features.management.domain.repository.PurchaseRepository
import com.wael.astimal.pos.features.management.domain.repository.SupplierRepository
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
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

class PurchaseViewModel(
    private val purchaseRepository: PurchaseRepository,
    private val supplierRepository: SupplierRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(PurchaseScreenState())
    val state: StateFlow<PurchaseScreenState> = _state.asStateFlow()
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            sessionManager.getCurrentUser().collect { user ->
                updateCurrentUser(user)
            }
        }
        onEvent(PurchaseScreenEvent.SearchPurchases(""))
        loadDropdownData()
    }

    private fun updateCurrentUser(user: com.wael.astimal.pos.features.user.domain.entity.User?) {
        _state.update {
            when {
                user == null -> it
                user.isAdmin -> it.copy(currentUser = user)
                else -> it.copy(
                    currentUser = user,
                    currentPurchaseInput = it.currentPurchaseInput.copy(selectedEmployeeId = user.id)
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

    fun onEvent(event: PurchaseScreenEvent) {
        when (event) {
            is PurchaseScreenEvent.SearchPurchases -> searchPurchases(event.query)
            is PurchaseScreenEvent.SelectPurchaseToView -> selectPurchaseToView(event.purchase)
            is PurchaseScreenEvent.UpdateIsQueryActive -> _state.update { it.copy(isQueryActive = event.isActive) }
            is PurchaseScreenEvent.UpdateQuery -> _state.update { it.copy(query = event.query) }
            is PurchaseScreenEvent.OpenNewPurchaseForm -> {
                _state.update { PurchaseScreenState(currentUser = state.value.currentUser) }
                updateCurrentUser(state.value.currentUser)
            }
            is PurchaseScreenEvent.SelectSupplier -> _state.update { it.copy(selectedSupplier = event.supplier) }
            is PurchaseScreenEvent.SelectEmployee -> updatePurchaseInput { it.copy(selectedEmployeeId = event.employeeId) }
            is PurchaseScreenEvent.UpdatePaymentType -> updatePurchaseInput { it.copy(paymentType = event.type ?: PaymentType.CASH) }
            is PurchaseScreenEvent.AddItemToPurchase -> updatePurchaseInput { it.copy(items = it.items + EditableItem()) }
            is PurchaseScreenEvent.RemoveItemFromPurchase -> updatePurchaseInput { it.copy(items = it.items.filterNot { item -> item.tempEditorId == event.tempEditorId }) }
            is PurchaseScreenEvent.UpdateItemProduct -> updatePurchaseItem(event.tempEditorId) {
                it.copy(
                    product = event.product,
                    price = event.product?.averagePrice?.toString() ?: "0.0",
                    selectedProductUnit = event.product?.minimumProductUnit
                )
            }
            is PurchaseScreenEvent.UpdateItemUnit -> updatePurchaseItem(event.tempEditorId) { it.copy(selectedProductUnit = event.productUnit) }
            is PurchaseScreenEvent.UpdateItemQuantity -> updatePurchaseItem(event.tempEditorId) { it.copy(quantity = event.quantity) }
            is PurchaseScreenEvent.UpdateItemPrice -> updatePurchaseItem(event.tempEditorId) { it.copy(price = event.price) }
            is PurchaseScreenEvent.SavePurchase -> savePurchase()
            is PurchaseScreenEvent.DeletePurchase -> deletePurchase()
            is PurchaseScreenEvent.ClearSnackbar -> _state.update { it.copy(snackbarMessage = null) }
            is PurchaseScreenEvent.ClearError -> _state.update { it.copy(error = null) }
            is PurchaseScreenEvent.UpdateAmountPaid -> updatePurchaseInput { it.copy(amountPaid = event.amountPaid) }
            is PurchaseScreenEvent.UpdateTransferDate -> updatePurchaseInput { it.copy(date = event.date ?: System.currentTimeMillis()) }
        }
    }

    private fun selectPurchaseToView(order: PurchaseOrder?) {
        _state.update {
            it.copy(
                selectedPurchase = order,
                isQueryActive = false,
                selectedSupplier = order?.supplier,
                currentPurchaseInput = if (order == null) EditableItemList(selectedEmployeeId = currentUserId)
                else EditableItemList(
                    selectedEmployeeId = order.user?.id,
                    paymentType = order.paymentType,
                    date = order.purchaseDate,
                    items = order.items.map { item ->
                        EditableItem(
                            tempEditorId = item.localId.toString(),
                            product = item.product,
                            selectedProductUnit = item.productUnit,
                            quantity = item.quantity.toString(),
                            price = item.purchasePrice.toString(),
                        )
                    }
                )
            )
        }
    }

    private fun updatePurchaseInput(action: (EditableItemList) -> EditableItemList) {
        _state.update { it.copy(currentPurchaseInput = action(it.currentPurchaseInput)) }
    }

    private fun updatePurchaseItem(
        tempId: String, action: (EditableItem) -> EditableItem
    ) {
        val currentItems = _state.value.currentPurchaseInput.items.toMutableList()
        val index = currentItems.indexOfFirst { it.tempEditorId == tempId }
        if (index != -1) {
            currentItems[index] = action(currentItems[index])
            updatePurchaseInput { it.copy(items = currentItems) }
        }
    }

    private fun searchPurchases(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, query = query) }
            delay(300)
            purchaseRepository.getPurchases()
                .catch { _state.update { it.copy(loading = false, error = R.string.error_searching_orders) } }
                .collect { purchases -> _state.update { it.copy(loading = false, purchases = purchases) } }
        }
    }

    private fun savePurchase() {
        val purchaseInput = _state.value.currentPurchaseInput
        val selectedSupplier = _state.value.selectedSupplier
        val currentUserID = currentUserId ?: run {
            _state.update { it.copy(error = R.string.user_not_identified) }; return
        }
        if (selectedSupplier == null || purchaseInput.items.isEmpty()) {
            _state.update { it.copy(error = R.string.supplier_and_at_least_one_item_are_required) }; return
        }
        val itemEntities = purchaseInput.items.mapNotNull {
            val quantity = it.quantity.toDoubleOrNull() ?: 0.0
            if (it.product == null || it.selectedProductUnit == null || quantity <= 0) return@mapNotNull null
            PurchaseProductEntity(
                productLocalId = it.product.localId,
                unitLocalId = it.selectedProductUnit.localId,
                quantity = quantity,
                purchasePrice = it.price.toDoubleOrNull() ?: 0.0,
                itemTotalPrice = quantity * (it.price.toDoubleOrNull() ?: 0.0),
                purchaseLocalId = 0L,
                serverId = null
            )
        }
        if (itemEntities.size != purchaseInput.items.size) {
            _state.update { it.copy(error = R.string.one_or_more_order_items_are_invalid) }; return
        }
        val purchaseEntity = PurchaseEntity(
            localId = _state.value.selectedPurchase?.localId ?: 0L,
            supplierLocalId = selectedSupplier.id,
            employeeLocalId = purchaseInput.selectedEmployeeId ?: currentUserID,
            totalPrice = purchaseInput.totalAmount,
            paymentType = purchaseInput.paymentType,
            purchaseDate = System.currentTimeMillis(),
            serverId = null,
            invoiceNumber = null,
        )
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val result = if (_state.value.isNew) purchaseRepository.addPurchase(
                purchaseEntity, itemEntities
            ) else purchaseRepository.updatePurchase(
                purchaseEntity, itemEntities
            )

            result.fold(onSuccess = {
                _state.update {
                    it.copy(
                        loading = false,
                        snackbarMessage = R.string.purchase_saved,
                        selectedPurchase = null,
                        currentPurchaseInput = EditableItemList(),
                    )
                }
            }, onFailure = { _state.update { it.copy(loading = false, error = R.string.something_went_wrong) } })
        }
    }

    private fun deletePurchase() {
        val purchaseToDelete = _state.value.selectedPurchase ?: return
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            purchaseRepository.deletePurchase(purchaseToDelete.localId).fold(onSuccess = {
                _state.update {
                    it.copy(
                        loading = false,
                        selectedPurchase = null,
                        snackbarMessage = R.string.purchase_deleted,
                        currentPurchaseInput = EditableItemList(),
                    )
                }
            }, onFailure = {
                _state.update {
                    it.copy(
                        loading = false, error = R.string.error_deleting_purchase
                    )
                }
            })
            onEvent(PurchaseScreenEvent.SearchPurchases(""))
        }
    }

    private val currentUserId: Long?
        get() = _state.value.currentUser?.id
}

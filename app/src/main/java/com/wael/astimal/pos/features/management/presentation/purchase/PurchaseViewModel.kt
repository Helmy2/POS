package com.wael.astimal.pos.features.management.presentation.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.management.data.entity.PurchaseEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseProductEntity
import com.wael.astimal.pos.features.management.domain.entity.EditableItem
import com.wael.astimal.pos.features.management.domain.entity.EditableItemList
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.PurchaseOrder
import com.wael.astimal.pos.features.management.domain.repository.PurchaseRepository
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

class PurchaseViewModel(
    private val purchaseRepository: PurchaseRepository,
    private val supplierRepository: SupplierRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(PurchaseState())
    val state: StateFlow<PurchaseState> = _state.asStateFlow()
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            sessionManager.getCurrentUser().collect { updateCurrentUser(it) }
        }
        onEvent(PurchaseEvent.SearchPurchases(""))
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
                    currentPurchaseInput = it.currentPurchaseInput.copy(selectedEmployeeId = user.id)
                )
            }
        }
    }

    fun onEvent(event: PurchaseEvent) {
        when (event) {
            is PurchaseEvent.SearchPurchases -> searchPurchases(event.query)
            is PurchaseEvent.SelectPurchaseToView -> updateSelectedPurchase(event.purchase)
            is PurchaseEvent.SelectSupplier -> _state.update { it.copy(selectedSupplier = event.supplier) }
            is PurchaseEvent.SelectEmployee -> updatePurchaseInput { it.copy(selectedEmployeeId = event.employeeId) }
            is PurchaseEvent.UpdatePaymentType -> updatePurchaseInput {
                it.copy(paymentType = event.type ?: PaymentType.CASH)
            }
            is PurchaseEvent.UpdateAmountPaid -> updatePurchaseInput { it.copy(amountPaid = event.amount) }
            is PurchaseEvent.AddItemToPurchase -> updatePurchaseInput { it.copy(items = it.items + EditableItem()) }
            is PurchaseEvent.RemoveItemFromPurchase -> updatePurchaseInput { it.copy(items = it.items.filterNot { item -> item.tempEditorId == event.tempEditorId }) }
            is PurchaseEvent.UpdateItemProduct -> updatePurchaseItem(event.tempEditorId) {
                val conversionFactor = event.product?.subUnitsPerMainUnit ?: 1.0
                it.copy(
                    product = event.product,
                    minUnitPrice = (event.product?.averagePrice?.div(conversionFactor)).toString(),
                    maxUnitPrice = event.product?.averagePrice.toString(),
                    minUnitQuantity = conversionFactor.toString(),
                    maxUnitQuantity = "1.0",
                )
            }
            is PurchaseEvent.UpdateItemUnit -> updatePurchaseItem(event.tempEditorId) {
                it.copy(isSelectedUnitIsMax = event.isMaxUnitSelected)
            }
            is PurchaseEvent.SavePurchase -> savePurchase()
            is PurchaseEvent.ClearSnackbar -> _state.update { it.copy(snackbarMessage = null) }
            is PurchaseEvent.UpdateIsQueryActive -> _state.update { it.copy(isQueryActive = event.isActive) }
            is PurchaseEvent.UpdateQuery -> _state.update { it.copy(query = event.query) }
            is PurchaseEvent.DeletePurchase -> deletePurchase()
            PurchaseEvent.OpenNewPurchaseForm -> clearState()
            PurchaseEvent.ClearError -> _state.update { it.copy(error = null) }
            is PurchaseEvent.UpdatePurchaseDate -> updatePurchaseInput {
                it.copy(date = event.date ?: System.currentTimeMillis())
            }
            is PurchaseEvent.UpdateItemMaxUnitPrice -> updatePurchaseItem(event.tempEditorId) {
                val conversionFactor = it.product?.subUnitsPerMainUnit ?: 1.0
                it.copy(
                    maxUnitPrice = event.price,
                    minUnitPrice = (event.price.toDoubleOrNull()?.div(conversionFactor))?.toString() ?: "0.0"
                )
            }
            is PurchaseEvent.UpdateItemMinUnitPrice -> updatePurchaseItem(event.tempEditorId) {
                val conversionFactor = it.product?.subUnitsPerMainUnit ?: 1.0
                it.copy(
                    minUnitPrice = event.price,
                    maxUnitPrice = (event.price.toDoubleOrNull()?.times(conversionFactor))?.toString() ?: "0.0"
                )
            }
            is PurchaseEvent.UpdateItemMaxUnitQuantity -> updatePurchaseItem(event.tempEditorId) {
                val conversionFactor = it.product?.subUnitsPerMainUnit ?: 1.0
                it.copy(
                    maxUnitQuantity = event.quantity,
                    minUnitQuantity = (event.quantity.toDoubleOrNull()?.times(conversionFactor))?.toString() ?: "0.0"
                )
            }
            is PurchaseEvent.UpdateItemMinUnitQuantity -> updatePurchaseItem(event.tempEditorId) {
                val conversionFactor = it.product?.subUnitsPerMainUnit ?: 1.0
                it.copy(
                    minUnitQuantity = event.quantity,
                    maxUnitQuantity = (event.quantity.toDoubleOrNull()?.div(conversionFactor))?.toString() ?: "0.0"
                )
            }
        }
    }

    private fun deletePurchase() {
        viewModelScope.launch {
            _state.value.selectedPurchase?.localId?.let {
                purchaseRepository.deletePurchase(it).fold(onSuccess = {
                    clearState(snackbarMessage = R.string.purchase_deleted)
                }, onFailure = { _state.update { it.copy(error = R.string.error_deleting_purchase) } })
            }
        }
    }

    private fun updateSelectedPurchase(purchase: PurchaseOrder?) {
        _state.update {
            it.copy(
                isQueryActive = false,
                selectedPurchase = purchase,
                selectedSupplier = purchase?.supplier,
                currentPurchaseInput = if (purchase == null) EditableItemList(
                    selectedEmployeeId = it.currentUser?.id,
                ) else EditableItemList(
                    selectedEmployeeId = purchase.user?.id,
                    paymentType = purchase.paymentType,
                    date = purchase.data,
                    items = purchase.items.map { item ->
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
                    amountPaid = purchase.amountPaid.toString(),
                )
            )
        }
    }

    private fun updatePurchaseInput(action: (EditableItemList) -> EditableItemList) {
        _state.update { it.copy(currentPurchaseInput = action(it.currentPurchaseInput)) }
    }

    private fun updatePurchaseItem(tempId: String, action: (EditableItem) -> EditableItem) {
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
                .catch {
                    _state.update {
                        it.copy(
                            loading = false,
                            error = R.string.error_searching_orders
                        )
                    }
                }
                .collect { purchases ->
                    _state.update {
                        it.copy(
                            loading = false,
                            purchases = purchases
                        )
                    }
                }
        }
    }

    private fun savePurchase() {
        val purchaseInput = _state.value.currentPurchaseInput
        val selectedSupplier = _state.value.selectedSupplier
        val loggedInEmployeeId = _state.value.currentUser?.id
        if (loggedInEmployeeId == null) {
            _state.update { it.copy(error = R.string.user_not_identified) }
            return
        }
        if (selectedSupplier == null || purchaseInput.items.isEmpty()) {
            _state.update { it.copy(error = R.string.supplier_and_at_least_one_item_are_required) }
            return
        }

        val itemEntities = purchaseInput.items.mapNotNull {
            val quantity = it.maxUnitQuantity.toDoubleOrNull() ?: 0.0
            if (it.product == null || quantity <= 0) return@mapNotNull null
            PurchaseProductEntity(
                productLocalId = it.product.localId,
                unitLocalId = it.product.maximumProductUnit.localId,
                quantity = quantity,
                purchasePrice = it.maxUnitPrice.toDoubleOrNull() ?: 0.0,
                itemTotalPrice = it.lineTotal,
                serverId = null,
                purchaseLocalId = 0L
            )
        }

        if (itemEntities.size != purchaseInput.items.size) {
            _state.update { it.copy(error = R.string.one_or_more_order_items_are_invalid) }
            return
        }

        val purchaseEntity = PurchaseEntity(
            localId = _state.value.selectedPurchase?.localId ?: 0L,
            serverId = null,
            invoiceNumber = null,
            supplierLocalId = selectedSupplier.id,
            employeeLocalId = purchaseInput.selectedEmployeeId ?: loggedInEmployeeId,
            amountPaid = purchaseInput.amountPaid.toDoubleOrNull() ?: 0.0,
            amountRemaining = purchaseInput.amountRemaining,
            totalAmount = purchaseInput.totalAmount,
            paymentType = purchaseInput.paymentType,
            purchaseDate = purchaseInput.date
        )

        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val result = if (_state.value.isNew) purchaseRepository.addPurchase(purchaseEntity, itemEntities)
            else purchaseRepository.updatePurchase(purchaseEntity, itemEntities)

            result.fold(onSuccess = {
                clearState(snackbarMessage = R.string.purchase_saved)
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
                selectedPurchase = null,
                selectedSupplier = null,
                currentPurchaseInput = EditableItemList(),
                isQueryActive = false,
                snackbarMessage = snackbarMessage,
            )
        }
        updateCurrentUser(state.value.currentUser)
    }
}

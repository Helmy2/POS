package com.wael.astimal.pos.features.management.presentaion.purchase

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.management.data.entity.PurchaseEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseProductEntity
import com.wael.astimal.pos.features.management.domain.entity.PurchaseOrder
import com.wael.astimal.pos.features.management.domain.repository.PurchaseRepository
import com.wael.astimal.pos.features.management.domain.repository.SupplierRepository
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
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
    private var currentUserId: Long? = null

    init {
        viewModelScope.launch {
            sessionManager.getCurrentUser().collect { user ->
                currentUserId = user?.id?.toLong()
                if (_state.value.currentPurchaseInput.selectedEmployeeId == null) {
                    _state.update { s ->
                        s.copy(
                            currentPurchaseInput = s.currentPurchaseInput.copy(
                                selectedEmployeeId = currentUserId
                            )
                        )
                    }
                }
            }
        }
        onEvent(PurchaseScreenEvent.SearchPurchases(""))
        loadDropdownData()
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
            is PurchaseScreenEvent.OpenNewPurchaseForm -> _state.update {
                it.copy(
                    selectedPurchase = null,
                    currentPurchaseInput = EditablePurchaseOrder(selectedEmployeeId = currentUserId)
                )
            }

            is PurchaseScreenEvent.SelectSupplier -> updatePurchaseInput { it.copy(selectedSupplier = event.supplier) }
            is PurchaseScreenEvent.UpdatePaymentType -> updatePurchaseInput { it.copy(paymentType = event.type) }
            is PurchaseScreenEvent.AddItemToPurchase -> updatePurchaseInput { it.copy(items = it.items + EditablePurchaseItem()) }
            is PurchaseScreenEvent.RemoveItemFromPurchase -> updatePurchaseInput { it.copy(items = it.items.filterNot { item -> item.tempEditorId == event.tempEditorId }) }
            is PurchaseScreenEvent.UpdateItemProduct -> updatePurchaseItem(event.tempEditorId) {
                val price = event.product?.averagePrice?.toString() ?: "0.0"
                it.copy(product = event.product, purchasePrice = price)
            }

            is PurchaseScreenEvent.UpdateItemUnit -> updatePurchaseItem(event.tempEditorId) {
                it.copy(
                    selectedUnit = event.unit
                )
            }

            is PurchaseScreenEvent.UpdateItemQuantity -> updatePurchaseItem(event.tempEditorId) {
                it.copy(
                    quantity = event.quantity
                )
            }

            is PurchaseScreenEvent.UpdateItemPrice -> updatePurchaseItem(event.tempEditorId) {
                it.copy(
                    purchasePrice = event.price
                )
            }

            is PurchaseScreenEvent.SavePurchase -> savePurchase()
            is PurchaseScreenEvent.DeletePurchase -> deletePurchase()
            is PurchaseScreenEvent.ClearSnackbar -> _state.update { it.copy(snackbarMessage = null) }
            is PurchaseScreenEvent.ClearError -> _state.update { it.copy(error = null) }
            is PurchaseScreenEvent.SelectEmployee -> updatePurchaseInput { it.copy(selectedEmployeeId = event.employeeId) }
        }
    }

    private fun selectPurchaseToView(order: PurchaseOrder?) {
        _state.update {
            it.copy(
                selectedPurchase = order,
                isQueryActive = false,
                currentPurchaseInput = if (order == null) EditablePurchaseOrder(selectedEmployeeId = currentUserId)
                else EditablePurchaseOrder(
                    selectedSupplier = order.supplier,
                    selectedEmployeeId = order.user?.id?.toLong(),
                    paymentType = order.paymentType,
                    items = order.items.map {
                        EditablePurchaseItem(
                            tempEditorId = it.localId.toString(),
                            product = it.product,
                            selectedUnit = it.unit,
                            quantity = it.quantity.toString(),
                            purchasePrice = it.purchasePrice.toString(),
                            lineTotal = it.itemTotalPrice,
                        )
                    },
                    totalPrice = order.totalPrice
                )
            )
        }
    }

    private fun updatePurchaseInput(action: (EditablePurchaseOrder) -> EditablePurchaseOrder) {
        _state.update { it.copy(currentPurchaseInput = action(it.currentPurchaseInput)) }
        recalculateTotals()
    }

    private fun updatePurchaseItem(
        tempId: String, action: (EditablePurchaseItem) -> EditablePurchaseItem
    ) {
        val currentItems = _state.value.currentPurchaseInput.items.toMutableList()
        val index = currentItems.indexOfFirst { it.tempEditorId == tempId }
        if (index != -1) {
            currentItems[index] = action(currentItems[index])
            updatePurchaseInput { it.copy(items = currentItems) }
        }
    }

    private fun recalculateTotals() {
        val purchaseInput = _state.value.currentPurchaseInput
        var total = 0.0
        val updatedItems = purchaseInput.items.map { item ->
            val quantity = item.quantity.toDoubleOrNull() ?: 0.0
            val price = item.purchasePrice.toDoubleOrNull() ?: 0.0
            val lineTotal = quantity * price
            total += lineTotal
            item.copy(lineTotal = lineTotal)
        }
        _state.update { s ->
            s.copy(
                currentPurchaseInput = s.currentPurchaseInput.copy(
                    items = updatedItems, totalPrice = total
                )
            )
        }
    }

    private fun searchPurchases(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, query = query) }
            delay(300)
            purchaseRepository.getPurchases() // Assuming repository handles query
                .catch { e ->
                    _state.update {
                        it.copy(
                            loading = false, error = R.string.error_searching_orders
                        )
                    }
                }.collect { purchases ->
                    _state.update {
                        it.copy(
                            loading = false, purchases = purchases
                        )
                    }
                }
        }
    }

    private fun savePurchase() {
        val purchaseInput = _state.value.currentPurchaseInput
        val currentUserID = currentUserId ?: run {
            _state.update { it.copy(error = R.string.user_not_identified) }; return
        }
        if (purchaseInput.selectedSupplier == null || purchaseInput.items.isEmpty()) {
            _state.update { it.copy(error = R.string.supplier_and_at_least_one_item_are_required) }; return
        }
        val itemEntities = purchaseInput.items.mapNotNull {
            if (it.product == null || it.selectedUnit == null || (it.quantity.toDoubleOrNull()
                    ?: 0.0) <= 0
            ) return@mapNotNull null
            PurchaseProductEntity(
                productLocalId = it.product.localId,
                unitLocalId = it.selectedUnit.localId,
                quantity = it.quantity.toDouble(),
                purchasePrice = it.purchasePrice.toDoubleOrNull() ?: 0.0,
                itemTotalPrice = it.lineTotal,
                purchaseLocalId = 0L,
                serverId = null
            )
        }
        if (itemEntities.size != purchaseInput.items.size) {
            _state.update { it.copy(error = R.string.one_or_more_order_items_are_invalid) }; return
        }
        val purchaseEntity = PurchaseEntity(
            localId = _state.value.selectedPurchase?.localId ?: 0L,
            supplierLocalId = purchaseInput.selectedSupplier.id,
            employeeLocalId = purchaseInput.selectedEmployeeId?.toLong() ?: currentUserID,
            totalPrice = purchaseInput.totalPrice, paymentType = purchaseInput.paymentType,
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
                        currentPurchaseInput = EditablePurchaseOrder(),
                    )
                }
            }, onFailure = { e ->
                Log.d("TAG", "savePurchase: $e")
                _state.update {
                    it.copy(
                        loading = false, error = R.string.something_went_wrong
                    )
                }
            })
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
                        currentPurchaseInput = EditablePurchaseOrder(),
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
}
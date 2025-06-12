package com.wael.astimal.pos.features.management.presentaion.purchase_return

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.management.data.entity.PurchaseReturnEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseReturnProductEntity
import com.wael.astimal.pos.features.management.domain.entity.PurchaseReturn
import com.wael.astimal.pos.features.management.domain.repository.PurchaseReturnRepository
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
    private var currentUserId: Long? = null

    init {
        viewModelScope.launch {
            sessionManager.getCurrentUser().collect { user ->
                currentUserId = user?.id
                if (_state.value.newReturnInput.selectedEmployeeId == null) {
                    _state.update { s ->
                        s.copy(
                            newReturnInput = s.newReturnInput.copy(
                                selectedEmployeeId = currentUserId
                            )
                        )
                    }
                }
            }
        }
        onEvent(PurchaseReturnScreenEvent.SearchReturns(""))
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

    fun onEvent(event: PurchaseReturnScreenEvent) {
        when (event) {
            is PurchaseReturnScreenEvent.SearchReturns -> searchReturns(event.query)
            is PurchaseReturnScreenEvent.SelectReturnToView -> selectReturn(event.purchaseReturn)
            is PurchaseReturnScreenEvent.UpdateIsQueryActive -> _state.update {
                it.copy(
                    isQueryActive = event.isActive
                )
            }

            is PurchaseReturnScreenEvent.UpdateQuery -> _state.update { it.copy(query = event.query) }
            is PurchaseReturnScreenEvent.OpenNewReturnForm -> _state.update {
                it.copy(
                    isQueryActive = false,
                    selectedReturn = null,
                    newReturnInput = EditablePurchaseReturn(selectedEmployeeId = currentUserId)
                )
            }

            is PurchaseReturnScreenEvent.SelectSupplier -> updateReturnInput {
                it.copy(
                    selectedSupplier = event.supplier
                )
            }

            is PurchaseReturnScreenEvent.UpdatePaymentType -> updateReturnInput {
                it.copy(
                    paymentType = event.type
                )
            }

            is PurchaseReturnScreenEvent.AddItemToReturn -> updateReturnInput { it.copy(items = it.items + EditablePurchaseReturnItem()) }
            is PurchaseReturnScreenEvent.RemoveItemFromReturn -> updateReturnInput { it.copy(items = it.items.filterNot { item -> item.tempEditorId == event.tempEditorId }) }
            is PurchaseReturnScreenEvent.UpdateItemProduct -> updateReturnItem(event.tempEditorId) {
                val price =
                    event.product?.averagePrice?.toString() ?: "0.0" // Default to average cost
                it.copy(product = event.product, purchasePrice = price)
            }

            is PurchaseReturnScreenEvent.UpdateItemUnit -> updateReturnItem(event.tempEditorId) {
                it.copy(
                    selectedProductUnit = event.productUnit
                )
            }

            is PurchaseReturnScreenEvent.UpdateItemQuantity -> updateReturnItem(event.tempEditorId) {
                it.copy(
                    quantity = event.quantity
                )
            }

            is PurchaseReturnScreenEvent.UpdateItemPrice -> updateReturnItem(event.tempEditorId) {
                it.copy(
                    purchasePrice = event.price
                )
            }

            is PurchaseReturnScreenEvent.SaveReturn -> saveReturn()
            is PurchaseReturnScreenEvent.DeleteReturn -> deleteReturn()
            is PurchaseReturnScreenEvent.ClearSnackbar -> _state.update { it.copy(snackbarMessage = null) }
            is PurchaseReturnScreenEvent.ClearError -> _state.update { it.copy(error = null) }
            is PurchaseReturnScreenEvent.SelectEmployee ->
                updateReturnInput {
                    it.copy(
                        selectedEmployeeId = event.id ?: currentUserId
                    )
                }
        }
    }

    private fun selectReturn(purchaseReturn: PurchaseReturn?) {
        _state.update { it.copy(selectedReturn = purchaseReturn, isQueryActive = false) }
        if (purchaseReturn == null) {
            _state.update { it.copy(newReturnInput = EditablePurchaseReturn(selectedEmployeeId = currentUserId)) }
        } else {
            _state.update {
                it.copy(
                    newReturnInput = EditablePurchaseReturn(
                        selectedSupplier = purchaseReturn.supplier,
                        selectedEmployeeId = purchaseReturn.employee?.id,
                        paymentType = purchaseReturn.paymentType,
                        items = purchaseReturn.items.map { item ->
                            EditablePurchaseReturnItem(
                                tempEditorId = item.localId.toString(),
                                product = item.product,
                                selectedProductUnit = item.productUnit,
                                quantity = item.quantity.toString(),
                                purchasePrice = item.purchasePrice.toString(),
                                lineTotal = item.itemTotalPrice
                            )
                        },
                        totalReturnedValue = purchaseReturn.totalPrice
                    )
                )
            }
        }
        recalculateTotals()
    }

    private fun updateReturnInput(action: (EditablePurchaseReturn) -> EditablePurchaseReturn) {
        _state.update { it.copy(newReturnInput = action(it.newReturnInput)) }
        recalculateTotals()
    }

    private fun updateReturnItem(
        tempId: String, action: (EditablePurchaseReturnItem) -> EditablePurchaseReturnItem
    ) {
        val currentItems = _state.value.newReturnInput.items.toMutableList()
        val index = currentItems.indexOfFirst { it.tempEditorId == tempId }
        if (index != -1) {
            currentItems[index] = action(currentItems[index])
            updateReturnInput { it.copy(items = currentItems) }
        }
    }

    private fun recalculateTotals() {
        val returnInput = _state.value.newReturnInput
        var total = 0.0
        val updatedItems = returnInput.items.map { item ->
            val quantity = item.quantity.toDoubleOrNull() ?: 0.0
            val price = item.purchasePrice.toDoubleOrNull() ?: 0.0
            val lineTotal = quantity * price
            total += lineTotal
            item.copy(lineTotal = lineTotal)
        }
        _state.update { s ->
            s.copy(
                newReturnInput = s.newReturnInput.copy(
                    items = updatedItems, totalReturnedValue = total
                )
            )
        }
    }

    private fun searchReturns(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, query = query) }
            delay(300)
            purchaseReturnRepository.getPurchaseReturns().catch { e ->
                    _state.update {
                        it.copy(
                            loading = false, error = R.string.error_searching_orders
                        )
                    }
                }.collect { returns ->
                    _state.update {
                        it.copy(
                            loading = false, returns = returns
                        )
                    }
                }
        }
    }

    private fun saveReturn() {
        val returnInput = _state.value.newReturnInput
        val employeeId = currentUserId ?: run {
            _state.update { it.copy(error = R.string.user_not_identified) }; return
        }
        if (returnInput.selectedSupplier == null || returnInput.items.isEmpty()) {
            _state.update { it.copy(error = R.string.supplier_and_at_least_one_item_are_required) }; return
        }
        val itemEntities = returnInput.items.mapNotNull {
            if (it.product == null || it.selectedProductUnit == null || (it.quantity.toDoubleOrNull()
                    ?: 0.0) <= 0
            ) return@mapNotNull null
            PurchaseReturnProductEntity(
                productLocalId = it.product.localId,
                unitLocalId = it.selectedProductUnit.localId,
                quantity = it.quantity.toDouble(),
                purchasePrice = it.purchasePrice.toDoubleOrNull() ?: 0.0,
                itemTotalPrice = it.lineTotal,
                purchaseReturnLocalId = 1,
                serverId = null,
            )
        }
        if (itemEntities.size != returnInput.items.size) {
            _state.update { it.copy(error = R.string.one_or_more_order_items_are_invalid) }; return
        }
        val returnEntity = PurchaseReturnEntity(
            localId = _state.value.selectedReturn?.localId ?: 0L,
            supplierLocalId = returnInput.selectedSupplier.id,
            employeeLocalId = returnInput.selectedEmployeeId ?: employeeId,
            totalPrice = returnInput.totalReturnedValue, paymentType = returnInput.paymentType,
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
                        newReturnInput = EditablePurchaseReturn()
                    )
                }
            }, onFailure = { e ->
                Log.d("TAG", "saveReturn: $e")
                _state.update {
                    it.copy(
                        loading = false, error = R.string.something_went_wrong
                    )
                }
            })
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
                            newReturnInput = EditablePurchaseReturn()
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
}
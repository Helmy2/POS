package com.wael.astimal.pos.features.inventory.presentation.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.features.inventory.data.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.repository.CategoryRepository
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.inventory.domain.repository.UnitRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val unitRepository: UnitRepository,
    private val storeRepository: StoreRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProductState())
    val state: StateFlow<ProductState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        searchProducts("")
        loadDropdownData()
    }

    private fun loadDropdownData() {
        viewModelScope.launch {
            categoryRepository.getCategories("").collect { categories ->
                _state.update { it.copy(categories = categories) }
            }
        }
        viewModelScope.launch {
            unitRepository.getUnits("").collect { units ->
                _state.update { it.copy(units = units) }
            }
        }
        viewModelScope.launch {
            storeRepository.getStores("").collect { stores ->
                _state.update { it.copy(stores = stores) }
            }
        }
    }

    fun onEvent(event: ProductEvent) {
        when (event) {
            is ProductEvent.SaveProduct -> saveProduct()
            is ProductEvent.DeleteProduct -> deleteSelectedProduct()
            is ProductEvent.Search -> searchProducts(event.query)
            is ProductEvent.SelectProduct -> handleSelectProduct(event.product)
            is ProductEvent.UpdateQuery -> _state.update { it.copy(query = event.query) }
            is ProductEvent.UpdateIsQueryActive -> _state.update { it.copy(isQueryActive = event.isQueryActive) }
            is ProductEvent.UpdateInputArName -> _state.update { it.copy(inputArName = event.name) }
            is ProductEvent.UpdateInputEnName -> _state.update { it.copy(inputEnName = event.name) }
            is ProductEvent.SelectCategoryId -> _state.update { it.copy(selectedCategoryId = event.id) }
            is ProductEvent.UpdateInputAveragePrice -> _state.update {
                it.copy(inputAveragePrice = event.price)
            }

            is ProductEvent.UpdateInputSellingPrice -> _state.update {
                it.copy(inputSellingPrice = event.price)
            }

            is ProductEvent.UpdateInputOpeningBalance -> _state.update {
                it.copy(inputOpeningBalance = event.qty)
            }

            is ProductEvent.SelectStoreId -> _state.update { it.copy(selectedStoreId = event.id) }

            is ProductEvent.SelectMinStockUnitId -> _state.update {
                it.copy(selectedMinStockUnitId = event.id)
            }

            is ProductEvent.SelectMaxStockUnitId -> _state.update {
                it.copy(selectedMaxStockUnitId = event.id)
            }

            is ProductEvent.UpdateSubUnitsPerMainUnit -> _state.update {
                it.copy(subUnitsPerMainUnit = event.value)
            }
        }
    }

    private fun searchProducts(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            if (query.length > 1 || query.isEmpty()) {
                delay(300)
            }
            productRepository.getProducts(query).catch { e ->
                _state.update {
                    it.copy(
                        loading = false, error = "Error fetching products: ${e.message}"
                    )
                }
            }.collect { products ->
                _state.update {
                    it.copy(
                        loading = false, searchResults = products
                    )
                }
            }
        }
    }

    private fun handleSelectProduct(product: Product?) {
        if (product == null) {
            _state.update {
                it.copy(
                    selectedProduct = null,
                    inputArName = "",
                    inputEnName = "",
                    selectedCategoryId = null,
                    inputAveragePrice = "",
                    inputSellingPrice = "",
                    inputOpeningBalance = "",
                    selectedStoreId = null,
                    selectedMinStockUnitId = null,
                    selectedMaxStockUnitId = null,
                )
            }
        } else {
            _state.update {
                it.copy(
                    selectedProduct = product,
                    inputArName = product.localizedName.arName ?: "",
                    inputEnName = product.localizedName.enName ?: "",
                    selectedCategoryId = product.category?.localId,
                    inputAveragePrice = product.averagePrice.toString(),
                    inputSellingPrice = product.sellingPrice.toString(),
                    inputOpeningBalance = product.openingBalanceQuantity?.toString() ?: "",
                    selectedStoreId = product.store?.localId,
                    selectedMinStockUnitId = product.minimumProductUnit?.localId,
                    selectedMaxStockUnitId = product.maximumProductUnit.localId,
                )
            }
        }
    }

    private fun saveProduct() {
        val currentState = _state.value
        if (currentState.inputArName.isBlank() && currentState.inputEnName.isBlank()) {
            _state.update { it.copy(error = "At least one product name is required.") }
            return
        }
        // Add more validation as needed (e.g., prices are numbers, category/unit selected)

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }

            // Construct ProductEntity from current state
            val productEntity = ProductEntity(
                localId = currentState.selectedProduct?.localId ?: 0L,
                serverId = currentState.selectedProduct?.serverId,
                arName = currentState.inputArName,
                enName = currentState.inputEnName,
                categoryId = currentState.selectedCategoryId,
                averagePrice = currentState.inputAveragePrice.toDoubleOrNull() ?: 0.0,
                sellingPrice = currentState.inputSellingPrice.toDoubleOrNull()?: 0.0,
                openingBalanceQuantity = currentState.inputOpeningBalance.toDoubleOrNull(),
                storeId = currentState.selectedStoreId,
                minimumUnitId = currentState.selectedMinStockUnitId,
                maximumUnitId = currentState.selectedMaxStockUnitId,
                isSynced = false,
                lastModified = System.currentTimeMillis(),
                subUnitsPerMainUnit = currentState.subUnitsPerMainUnit.toDoubleOrNull() ?: 1.0,
            )

            val result = if (currentState.isNew || currentState.selectedProduct == null) {
                productRepository.addProduct(productEntity)
            } else {
                productRepository.updateProduct(productEntity)
            }

            result.fold(onSuccess = {
                _state.update {
                    it.copy(
                        loading = false,
                        selectedProduct = null,
                        inputArName = "",
                        inputEnName = "",
                        selectedCategoryId = null,
                        inputAveragePrice = "",
                        inputSellingPrice = "",
                        inputOpeningBalance = "",
                        selectedStoreId = null,
                        selectedMinStockUnitId = null,
                        selectedMaxStockUnitId = null,
                    )
                }
            }, onFailure = { e ->
                _state.update {
                    it.copy(
                        loading = false, error = "Failed to save product: ${e.message}"
                    )
                }
            })
        }
    }

    private fun deleteSelectedProduct() {
        val productToDelete = _state.value.selectedProduct ?: return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val result = productRepository.deleteProduct(productToDelete.localId)
            result.fold(onSuccess = {
                _state.update {
                    it.copy(
                        loading = false,
                        selectedProduct = null,
                        inputArName = "",
                        inputEnName = "",
                        selectedCategoryId = null,
                        inputAveragePrice = "",
                        inputSellingPrice = "",
                        inputOpeningBalance = "",
                        selectedStoreId = null,
                        selectedMinStockUnitId = null,
                        selectedMaxStockUnitId = null,
                    )
                }
            }, onFailure = { e ->
                _state.update {
                    it.copy(
                        loading = false, error = "Failed to delete product: ${e.message}"
                    )
                }
            })
        }
    }
}
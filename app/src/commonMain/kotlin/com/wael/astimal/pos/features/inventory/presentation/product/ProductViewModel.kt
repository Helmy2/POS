package com.wael.astimal.pos.features.inventory.presentation.product

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustment
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import com.wael.astimal.pos.features.inventory.domain.repository.CategoryRepository
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.inventory.domain.repository.UnitRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pos.app.generated.resources.Res
import pos.app.generated.resources.error_some_field_are_required
import pos.app.generated.resources.failed_to_delete_product
import pos.app.generated.resources.failed_to_load_categories
import pos.app.generated.resources.failed_to_load_products
import pos.app.generated.resources.failed_to_load_stores
import pos.app.generated.resources.failed_to_load_units
import pos.app.generated.resources.failed_to_save_product
import pos.app.generated.resources.product_deleted_successfully
import pos.app.generated.resources.product_saved_successfully

class ProductViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val unitRepository: UnitRepository,
    private val storeRepository: StoreRepository,
    private val userRepository: UserRepository,
    private val stockRepository: StockRepository,
    private val snackbarController: SnackbarController,
) : BaseViewModel<ProductReducer.State, ProductReducer.Event, Nothing>(
    reducer = ProductReducer(), initialState = ProductReducer.State()
) {
    private var searchJob: Job? = null

    init {
        loadCurrentUser()
        loadDropdownData()
        searchProducts("") // Initial load
    }

    override fun handleEvent(event: ProductReducer.Event) {
        when (event) {
            is ProductReducer.Event.SearchQueryChanged -> {
                setState(event)
                searchProducts(event.query)
            }

            is ProductReducer.Event.SaveClicked -> saveProduct()
            is ProductReducer.Event.DeleteConfirmed -> deleteProduct()
            else -> setState(event)
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            setState(ProductReducer.Event.UserLoaded(userRepository.getCurrentUser()))
        }
    }

    private fun loadDropdownData() {
        viewModelScope.launch {
            combine(
                categoryRepository.getCategories("").catch {
                    setState(ProductReducer.Event.LoadingFinished)
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_load_categories)))
                },
                unitRepository.getUnits("").catch {
                    setState(ProductReducer.Event.LoadingFinished)
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_load_units)))
                },
                storeRepository.getStores("").catch {
                    setState(ProductReducer.Event.LoadingFinished)
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_load_stores)))
                }
            ) { categories, units, stores ->
                ProductReducer.DropdownData(
                    categories,
                    units,
                    stores,
                )
            }.collect { dropdownData ->
                setState(ProductReducer.Event.DropdownDataLoaded(dropdownData))
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun searchProducts(query: String) {
        searchJob?.cancel()
        setState(ProductReducer.Event.LoadingStarted)
        searchJob = productRepository.getProducts(query).debounce(300L)
            .catch {
                setState(ProductReducer.Event.LoadingFinished)
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_load_products)))
            }
            .map {
                it.map { product ->
                    ProductReducer.ProductWithStock(
                        product,
                        stockRepository.getStockInCurrentStore(product.id)
                    )
                }
            }
            .onEach { products ->
                setState(ProductReducer.Event.ProductsLoaded(products))
            }.launchIn(viewModelScope)
    }

    private fun saveProduct() {
        viewModelScope.launch {
            val currentState = state.value
            if (!currentState.canSave) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_some_field_are_required)))
                return@launch
            }
            setState(ProductReducer.Event.LoadingStarted)
            viewModelScope.launch {
                val productToSave = Product(
                    id = currentState.selectedProduct?.product?.id ?: "",
                    name = LocalizedString(currentState.inputArName, currentState.inputEnName),
                    averagePrice = currentState.selectedProduct?.product?.averagePrice ?: 0.0,
                    sellingPrice = currentState.inputSellingPrice.toDoubleOrNull() ?: 0.0,
                    subUnitsPerMainUnit = currentState.inputSubUnitsPerMainUnit.toDoubleOrNull()
                        ?: 1.0,
                    category = currentState.dropdownData.categories.find { it.id == currentState.selectedCategory?.id }!!,
                    mainProductUnit = currentState.dropdownData.units.find { it.id == currentState.selectedMainUnit?.id }!!,
                    subProductUnit = currentState.dropdownData.units.find { it.id == currentState.selectedSubUnit?.id },
                    createdAt = currentState.selectedProduct?.product?.createdAt ?: Clock.now(),
                    purchasePrice = currentState.inputPurchasePrice.toDoubleOrNull() ?: 0.0,
                    barcode = "",
                )

                val result = productRepository.saveProduct(productToSave)

                result.onSuccess {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.product_saved_successfully)))
                    if (!state.value.isEditing && state.value.initialStore != null && (state.value.initialQuantity.toDoubleOrNull()
                            ?: 0.0) > 0.0
                    ) {
                        stockRepository.addStockAdjustment(
                            StockAdjustment(
                                store = state.value.initialStore!!,
                                product = it,
                                user = state.value.currentUser!!,
                                reason = StockAdjustmentReason.OPENING_BALANCE,
                                notes = null,
                                quantityChange = state.value.initialQuantity.toDoubleOrNull()
                                    ?: 0.0,
                                createdAt = Clock.now(),
                                invoiceId = null,
                                transactionId = null,
                                id = "",
                            )
                        )
                    }
                    setState(ProductReducer.Event.SaveSucceeded)
                    searchProducts("")
                }.onFailure {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_save_product)))
                    setState(ProductReducer.Event.LoadingFinished)
                }
            }
        }
    }

    private fun deleteProduct() {
        setState(ProductReducer.Event.DeleteConfirmed)
        val productToDelete = state.value.selectedProduct ?: return
        setState(ProductReducer.Event.LoadingStarted)
        viewModelScope.launch {
            productRepository.deleteProduct(productToDelete.product).onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.product_deleted_successfully)))
                setState(ProductReducer.Event.DeleteSucceeded)
                searchProducts("")
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_delete_product)))
                setState(ProductReducer.Event.LoadingFinished)
            }
        }
    }
}

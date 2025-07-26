package com.wael.astimal.pos.features.inventory.presentation.product

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.entity.Product
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
    private val navigationController: NavigationController
) : BaseViewModel<ProductContract.State, ProductContract.Event, Nothing>(
    reducer = ProductReducer(), initialState = ProductContract.State()
) {
    private var searchJob: Job? = null

    init {
        loadCurrentUser()
        loadDropdownData()
        searchProducts("") // Initial load
    }

    override fun handleEvent(event: ProductContract.Event) {
        when (event) {
            is ProductContract.Event.SearchQueryChanged -> {
                setState(event)
                searchProducts(event.query)
            }

            is ProductContract.Event.SaveClicked -> saveProduct()
            is ProductContract.Event.DeleteConfirmed -> deleteProduct()
            is ProductContract.Event.BackClicked -> navigateBack()
            else -> setState(event)
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            setState(ProductContract.Event.UserLoaded(userRepository.getCurrentUser()))
        }
    }

    private fun loadDropdownData() {
        viewModelScope.launch {
            combine(
                categoryRepository.getCategories("").catch {
                    setState(ProductContract.Event.LoadingFinished)
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_load_categories)))
                },
                unitRepository.getUnits("").catch {
                    setState(ProductContract.Event.LoadingFinished)
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_load_units)))
                },
                storeRepository.getStores("").catch {
                    setState(ProductContract.Event.LoadingFinished)
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_load_stores)))
                }
            ) { categories, units, stores ->
                ProductContract.DropdownData(
                    categories,
                    units,
                    stores,
                )
            }.collect { dropdownData ->
                setState(ProductContract.Event.DropdownDataLoaded(dropdownData))
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun searchProducts(query: String) {
        searchJob?.cancel()
        setState(ProductContract.Event.LoadingStarted)
        searchJob = productRepository.getProducts(query).debounce(300L)
            .catch {
                setState(ProductContract.Event.LoadingFinished)
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_load_products)))
            }
            .map {
                it.map { product ->
                    ProductContract.ProductWithStock(
                        product,
                        stockRepository.getStockInCurrentStore(product.id)
                    )
                }
            }
            .onEach { products ->
                setState(ProductContract.Event.ProductsLoaded(products))
            }.launchIn(viewModelScope)
    }

    private fun saveProduct() {
        viewModelScope.launch {
            val currentState = state.value
            if (!currentState.canSave) {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.error_some_field_are_required)))
                return@launch
            }
            setState(ProductContract.Event.LoadingStarted)
            viewModelScope.launch {
                val productToSave = Product(
                    id = currentState.selectedProduct?.product?.id ?: "",
                    name = LocalizedString(currentState.inputArName, currentState.inputEnName),
                    averagePrice = currentState.selectedProduct?.product?.averagePrice ?: 0.0,
                    sellingPrice = currentState.inputSellingPrice.toDouble(),
                    subUnitsPerMainUnit = currentState.inputSubUnitsPerMainUnit.toDoubleOrNull()
                        ?: 1.0,
                    category = currentState.dropdownData.categories.find { it.id == currentState.selectedCategoryId }!!,
                    mainProductUnit = currentState.dropdownData.units.find { it.id == currentState.selectedMainUnitId }!!,
                    subProductUnit = currentState.dropdownData.units.find { it.id == currentState.selectedSubUnitId },
                    createdAt = currentState.selectedProduct?.product?.createdAt ?: Clock.now(),
                    purchasePrice = currentState.inputPurchasePrice.toDouble(),
                    barcode = "",
                )

                val result = productRepository.saveProduct(productToSave)

                result.onSuccess {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.product_saved_successfully)))
                    setState(ProductContract.Event.SaveSucceeded)
                    searchProducts("")
                }.onFailure {
                    snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_save_product)))
                    setState(ProductContract.Event.LoadingFinished)
                }
            }
        }
    }

    private fun deleteProduct() {
        setState(ProductContract.Event.DeleteConfirmed)
        val productToDelete = state.value.selectedProduct ?: return
        setState(ProductContract.Event.LoadingStarted)
        viewModelScope.launch {
            productRepository.deleteProduct(productToDelete.product).onSuccess {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.product_deleted_successfully)))
                setState(ProductContract.Event.DeleteSucceeded)
                searchProducts("")
            }.onFailure {
                snackbarController.sendEvent(SnackbarEvent(StringResource.FromResource(Res.string.failed_to_delete_product)))
                setState(ProductContract.Event.LoadingFinished)
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            navigationController.navigateBack()
        }
    }
}

package com.wael.astimal.pos.features.inventory.presentation.product

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.util.SHOULD_SHOW_SHEATH_ON_START
import com.wael.astimal.pos.core.util.formate
import com.wael.astimal.pos.features.inventory.domain.entity.Category
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.user.domain.entity.User

class ProductReducer : Reducer<ProductReducer.State, ProductReducer.Event, Nothing> {
    data class ProductWithStock(
        val product: Product,
        val stock: Double
    )

    data class DropdownData(
        val categories: List<Category> = emptyList(),
        val units: List<ProductUnit> = emptyList(),
        val stores: List<Store> = emptyList()
    )

    data class State(
        val isLoading: Boolean = false,
        val products: List<ProductWithStock> = emptyList(),
        val selectedProduct: ProductWithStock? = null,
        val searchQuery: String = "",
        val isSearchActive: Boolean = SHOULD_SHOW_SHEATH_ON_START,
        val currentUser: User? = null,
        val dropdownData: DropdownData = DropdownData(),
        // Form input state
        val inputArName: String = "",
        val inputEnName: String = "",
        val inputPurchasePrice: String = "",
        val inputSellingPrice: String = "",
        val inputSubUnitsPerMainUnit: String = "1",
        val selectedCategory: Category? = null,
        val selectedMainUnit: ProductUnit? = null,
        val selectedSubUnit: ProductUnit? = null,
        val showDeleteDialog: Boolean = false

    ) : Reducer.ViewState {
        val isEditing: Boolean get() = selectedProduct != null
        val canUserEdit: Boolean get() = currentUser?.isAdmin == true
        val canSave: Boolean
            get() = inputArName.isNotBlank() &&
                    selectedCategory != null &&
                    selectedMainUnit != null
    }

    sealed interface Event : Reducer.ViewEvent {
        // UI Actions
        data class SearchQueryChanged(val query: String) : Event
        data class SearchActiveChanged(val isActive: Boolean) : Event
        data class ProductSelected(val product: ProductWithStock) : Event
        data object NewProductClicked : Event
        data object SaveClicked : Event
        data object DeleteClicked : Event
        data object DeleteConfirmed : Event
        data object DeleteCanceled : Event

        // Form Input Changes
        data class ArNameChanged(val name: String) : Event
        data class EnNameChanged(val name: String) : Event
        data class PurchasePriceChanged(val price: String) : Event
        data class SellingPriceChanged(val price: String) : Event
        data class SubUnitsPerMainUnitChanged(val value: String) : Event
        data class CategoryIdChanged(val category: Category?) : Event
        data class MainUnitIdChanged(val unit: ProductUnit?) : Event
        data class SubUnitIdChanged(val unit: ProductUnit?) : Event

        // Data results from ViewModel
        data class UserLoaded(val user: User?) : Event
        data class DropdownDataLoaded(val data: DropdownData) : Event
        data class ProductsLoaded(val products: List<ProductWithStock>) : Event
        data object SaveSucceeded : Event
        data object DeleteSucceeded : Event
        data object LoadingStarted : Event
        data object LoadingFinished : Event
    }
    
    override fun reduce(
        previousState: State,
        event: Event
    ): Pair<State, Nothing?> {
        return when (event) {
            is Event.LoadingStarted ->
                previousState.copy(isLoading = true) to null

            is Event.LoadingFinished ->
                previousState.copy(isLoading = false) to null

            is Event.SearchQueryChanged ->
                previousState.copy(searchQuery = event.query) to null

            is Event.SearchActiveChanged ->
                previousState.copy(isSearchActive = event.isActive) to null

            is Event.UserLoaded ->
                previousState.copy(currentUser = event.user) to null

            is Event.DropdownDataLoaded ->
                previousState.copy(dropdownData = event.data) to null

            is Event.ProductsLoaded ->
                previousState.copy(isLoading = false, products = event.products) to null

            is Event.ProductSelected ->
                previousState.copy(
                    selectedProduct = event.product,
                    inputArName = event.product.product.name.arName ?: "",
                    inputEnName = event.product.product.name.enName ?: "",
                    inputPurchasePrice = event.product.product.purchasePrice.formate(),
                    inputSellingPrice = event.product.product.sellingPrice.formate(),

                    inputSubUnitsPerMainUnit = event.product.product.subUnitsPerMainUnit.formate(),
                    selectedCategory = event.product.product.category,
                    // remove the selected store id
                    selectedSubUnit = event.product.product.subProductUnit,
                    selectedMainUnit = event.product.product.mainProductUnit,
                    isSearchActive = false
                ) to null

            is Event.NewProductClicked,
            is Event.SaveSucceeded,
            is Event.DeleteSucceeded ->
                // Clear the form
                previousState.copy(
                    isLoading = false,
                    selectedProduct = null,
                    inputArName = "",
                    inputEnName = "",
                    inputPurchasePrice = "",
                    inputSellingPrice = "",
                    inputSubUnitsPerMainUnit = "1",
                    selectedCategory = null,
                    selectedMainUnit = null,
                    selectedSubUnit = null
                ) to null

            // Form input updates
            is Event.ArNameChanged -> previousState.copy(inputArName = event.name) to null
            is Event.EnNameChanged -> previousState.copy(inputEnName = event.name) to null
            is Event.PurchasePriceChanged -> previousState.copy(inputPurchasePrice = event.price) to null
            is Event.SellingPriceChanged -> previousState.copy(inputSellingPrice = event.price) to null
            is Event.SubUnitsPerMainUnitChanged -> previousState.copy(
                inputSubUnitsPerMainUnit = event.value
            ) to null

            is Event.CategoryIdChanged -> previousState.copy(selectedCategory = event.category) to null
            is Event.SubUnitIdChanged -> previousState.copy(
                selectedSubUnit = event.unit
            ) to null

            is Event.MainUnitIdChanged -> previousState.copy(
                selectedMainUnit = event.unit
            ) to null

            is Event.DeleteClicked -> previousState.copy(
                showDeleteDialog = true
            ) to null

            is Event.DeleteCanceled, is Event.DeleteConfirmed -> previousState.copy(
                showDeleteDialog = false
            ) to null

            is Event.SaveClicked
                -> previousState to null
        }
    }
}

package com.wael.astimal.pos.features.inventory.presentation.product

import com.wael.astimal.pos.core.base.mvi.Reducer
import com.wael.astimal.pos.core.util.SHOULD_SHOW_SHEATH_ON_START
import com.wael.astimal.pos.features.inventory.domain.entity.Category
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.user.domain.entity.User

object ProductContract {

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
        val selectedCategoryId: String? = null,
        val selectedMainUnitId: String? = null,
        val selectedSubUnitId: String? = null,
        val showDeleteDialog: Boolean = false

    ) : Reducer.ViewState {
        val isEditing: Boolean get() = selectedProduct != null
        val canUserEdit: Boolean get() = currentUser?.isAdmin == true
        val canSave: Boolean
            get() = inputArName.isNotBlank() &&
                    inputPurchasePrice.isNotBlank() &&
                    inputSellingPrice.isNotBlank() &&
                    selectedCategoryId != null &&
                    selectedMainUnitId != null
    }

    sealed interface Event : Reducer.ViewEvent {
        // UI Actions
        data class SearchQueryChanged(val query: String) : Event
        data class SearchActiveChanged(val isActive: Boolean) : Event
        data class ProductSelected(val product: ProductWithStock) : Event
        data object NewProductClicked : Event
        data object SaveClicked : Event
        data object DeleteClicked : Event
        data object BackClicked : Event
        data object DeleteConfirmed : Event
        data object DeleteCanceled : Event

        // Form Input Changes
        data class ArNameChanged(val name: String) : Event
        data class EnNameChanged(val name: String) : Event
        data class PurchasePriceChanged(val price: String) : Event
        data class SellingPriceChanged(val price: String) : Event
        data class SubUnitsPerMainUnitChanged(val value: String) : Event
        data class CategoryIdChanged(val id: String?) : Event
        data class MainUnitIdChanged(val id: String?) : Event
        data class SubUnitIdChanged(val id: String?) : Event

        // Data results from ViewModel
        data class UserLoaded(val user: User?) : Event
        data class DropdownDataLoaded(val data: DropdownData) : Event
        data class ProductsLoaded(val products: List<ProductWithStock>) : Event
        data object SaveSucceeded : Event
        data object DeleteSucceeded : Event
        data object LoadingStarted : Event
        data object LoadingFinished : Event
    }
}
package com.wael.astimal.pos.features.inventory.presentation.product

import com.wael.astimal.pos.features.inventory.domain.entity.Category
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.user.domain.entity.User

data class ProductState(
    val loading: Boolean = false,
    val searchResults: List<Product> = emptyList(),
    val selectedProduct: Product? = null,
    val currentUser: User? = null,

    // Input fields for Add/Edit
    val inputArName: String = "",
    val inputEnName: String = "",
    val selectedCategoryId: Long? = null,
    val inputAveragePrice: String = "",
    val inputSellingPrice: String = "",
    val inputOpeningBalance: String = "",
    val selectedStoreId: Long? = null,
    val selectedMinStockUnitId: Long? = null,
    val selectedMaxStockUnitId: Long? = null,
    val subUnitsPerMainUnit: String = "",

    // Lists for dropdowns/pickers
    val categories: List<Category> = emptyList(),
    val units: List<ProductUnit> = emptyList(),
    val stores: List<Store> = emptyList(),

    val query: String = "",
    val isQueryActive: Boolean = false,
) {
    val isNew: Boolean get() = selectedProduct == null
    val canEdit get() = currentUser?.isAdmin == true
}

sealed interface ProductEvent {
    data object SaveProduct : ProductEvent
    data object DeleteProduct : ProductEvent
    data class UpdateInputArName(val name: String) : ProductEvent
    data class UpdateInputEnName(val name: String) : ProductEvent
    data class SelectCategoryId(val id: Long?) : ProductEvent
    data class UpdateInputAveragePrice(val price: String) : ProductEvent
    data class UpdateInputSellingPrice(val price: String) : ProductEvent
    data class UpdateInputOpeningBalance(val qty: String) : ProductEvent
    data class SelectStoreId(val id: Long?) : ProductEvent
    data class SelectMinStockUnitId(val unit: ProductUnit) : ProductEvent
    data class SelectMaxStockUnitId(val unit: ProductUnit) : ProductEvent
    data class UpdateQuery(val query: String) : ProductEvent
    data class UpdateIsQueryActive(val isQueryActive: Boolean) : ProductEvent
    data class Search(val query: String) : ProductEvent
    data class SelectProduct(val product: Product?) : ProductEvent
    data class UpdateSubUnitsPerMainUnit(val value: String) : ProductEvent
}
package com.wael.astimal.pos.features.inventory.presentation.product

import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.domain.entity.Category
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.Store

data class ProductState(
    val loading: Boolean = false,
    val searchResults: List<Product> = emptyList(),
    val selectedProduct: Product? = null,

    // Input fields for Add/Edit
    val inputArName: String = "",
    val inputEnName: String = "",
    val selectedCategoryId: Long? = null,
    val selectedUnitId: Long? = null,
    val inputAveragePrice: String = "",
    val inputSellingPrice: String = "",
    val inputOpeningBalance: String = "",
    val selectedStoreId: Long? = null,
    val inputMinStockLevel: String = "",
    val selectedMinStockUnitId: Long? = null,
    val inputMaxStockLevel: String = "",
    val selectedMaxStockUnitId: Long? = null,
    val inputFirstPeriodData: String = "",

    // Lists for dropdowns/pickers
    val categories: List<Category> = emptyList(),
    val units: List<UnitEntity> = emptyList(),
    val stores: List<Store> = emptyList(),

    val query: String = "",
    val isQueryActive: Boolean = false,
    val error: String? = null,
) {
    val isNew: Boolean get() = selectedProduct == null
}

sealed interface ProductEvent {
    data object SaveProduct : ProductEvent
    data object DeleteProduct : ProductEvent
    data class UpdateInputArName(val name: String) : ProductEvent
    data class UpdateInputEnName(val name: String) : ProductEvent
    data class SelectCategoryId(val id: Long?) : ProductEvent
    data class SelectUnitId(val id: Long?) : ProductEvent
    data class UpdateInputAveragePrice(val price: String) : ProductEvent
    data class UpdateInputSellingPrice(val price: String) : ProductEvent
    data class UpdateInputOpeningBalance(val qty: String) : ProductEvent
    data class SelectStoreId(val id: Long?) : ProductEvent
    data class UpdateInputMinStockLevel(val level: String) : ProductEvent
    data class SelectMinStockUnitId(val id: Long?) : ProductEvent
    data class UpdateInputMaxStockLevel(val level: String) : ProductEvent
    data class SelectMaxStockUnitId(val id: Long?) : ProductEvent
    data class UpdateInputFirstPeriodData(val data: String) : ProductEvent
    data class UpdateQuery(val query: String) : ProductEvent
    data class UpdateIsQueryActive(val isQueryActive: Boolean) : ProductEvent
    data class Search(val query: String) : ProductEvent
    data class SelectProduct(val product: Product?) : ProductEvent
}
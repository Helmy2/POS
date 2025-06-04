package com.wael.astimal.pos.features.inventory.presentation.category

import com.wael.astimal.pos.features.inventory.domain.entity.Category

data class CategoryScreenState(
    val loading: Boolean = false,
    val searchResults: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val inputArName: String = "",
    val inputEnName: String = "",
    val query: String = "",
    val isQueryActive: Boolean = false,
    val error: String? = null,
) {
    val isNew: Boolean get() = selectedCategory == null
}
sealed interface CategoryScreenEvent {
    data object CreateCategory : CategoryScreenEvent
    data object UpdateCategory : CategoryScreenEvent
    data object DeleteCategory : CategoryScreenEvent
    data class UpdateInputArName(val name: String) : CategoryScreenEvent
    data class UpdateInputEnName(val name: String) : CategoryScreenEvent
    data class UpdateQuery(val query: String) : CategoryScreenEvent
    data class UpdateIsQueryActive(val isQueryActive: Boolean) : CategoryScreenEvent
    data class Search(val query: String) : CategoryScreenEvent
    data class SelectCategory(val category: Category?) : CategoryScreenEvent
}


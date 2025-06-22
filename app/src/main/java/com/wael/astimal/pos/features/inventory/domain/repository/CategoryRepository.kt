package com.wael.astimal.pos.features.inventory.domain.repository

import com.wael.astimal.pos.features.inventory.data.entity.CategoryEntity
import com.wael.astimal.pos.features.inventory.domain.entity.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(query: String = ""): Flow<List<Category>>
    suspend fun saveCategory(category: CategoryEntity): Result<Unit>
    suspend fun deleteCategory(category: Category): Result<Unit>
}
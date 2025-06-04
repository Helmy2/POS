package com.wael.astimal.pos.features.inventory.domain.repository

import com.wael.astimal.pos.features.inventory.domain.entity.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(query: String): Flow<List<Category>>
    suspend fun getCategoryByLocalId(localId: Long): Category?
    suspend fun getCategoryByServerId(serverId: Int): Category?
    suspend fun addCategory(arName: String?, enName: String?): Result<Unit>
    suspend fun updateCategory(category: Category, newArName: String?, newEnName: String?): Result<Category>
    suspend fun deleteCategory(category: Category): Result<Unit>
    suspend fun syncCategories(): Result<Unit>
}
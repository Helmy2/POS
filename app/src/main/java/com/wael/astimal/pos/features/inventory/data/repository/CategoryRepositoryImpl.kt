package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.features.inventory.data.entity.CategoryEntity
import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.local.dao.CategoryDao
import com.wael.astimal.pos.features.inventory.domain.entity.Category
import com.wael.astimal.pos.features.inventory.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
) : CategoryRepository {


    override fun getCategories(query: String): Flow<List<Category>> {
        return categoryDao.searchCategoriesFlow(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveCategory(
        category: CategoryEntity
    ): Result<Unit> {
        return runCatching {
            if (category.enName.isNullOrBlank() && category.arName.isNullOrBlank()) {
                return Result.failure(IllegalArgumentException("At least one name (Arabic or English) must be provided for the category."))
            }

            categoryDao.insertOrUpdate(category)
        }
    }

    override suspend fun deleteCategory(category: Category): Result<Unit> {
        return try {
            val entityToDelete =
                categoryDao.getCategoryByLocalId(category.id.local) ?: return Result.failure(
                    NoSuchElementException("Category not found for deletion")
                )

            val categoryToMarkAsDeleted = entityToDelete.copy(
                isDeletedLocally = true, isSynced = false, updatedAt = System.currentTimeMillis()
            )
            categoryDao.insertOrUpdate(categoryToMarkAsDeleted)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
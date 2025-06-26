package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.local.dao.CategoryDao
import com.wael.astimal.pos.features.inventory.data.remote.dto.CategoryDto
import com.wael.astimal.pos.features.inventory.data.remote.dto.toEntity
import com.wael.astimal.pos.features.inventory.domain.entity.Category
import com.wael.astimal.pos.features.inventory.domain.entity.toEntity
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
        category: Category
    ): Result<Unit> {
        return runCatching {
            if (category.name.arName.isNullOrBlank() && category.name.enName.isNullOrBlank()) {
                return Result.failure(IllegalArgumentException("At least one name (Arabic or English) must be provided for the category."))
            }

            categoryDao.insertOrUpdate(category.toEntity())
        }
    }

    override suspend fun deleteCategory(category: Category): Result<Unit> {
        return try {
            val categoryToMarkAsDeleted = category.toEntity().copy(
                isDeletedLocally = true, isSynced = false, updatedAt = Clock.now()
            )
            categoryDao.insertOrUpdate(categoryToMarkAsDeleted)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncWithServer(categoriesDto: List<CategoryDto>) {
        val entities = categoriesDto.map { dto ->
            val existingEntity = categoryDao.getCategoryByServerId(dto.id)
            dto.toEntity().copy(
                localId = existingEntity?.localId ?: 0L
            )
        }
        categoryDao.upsertAll(entities)
    }
}
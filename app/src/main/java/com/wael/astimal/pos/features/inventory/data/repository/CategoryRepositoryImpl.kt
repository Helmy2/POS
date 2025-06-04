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

    override suspend fun getCategoryByLocalId(localId: Long): Category? {
        val entity = categoryDao.getCategoryByLocalId(localId)
        return if (entity?.isDeletedLocally == true) null else entity?.toDomain()
    }

    override suspend fun getCategoryByServerId(serverId: Int): Category? {
        val entity = categoryDao.getCategoryByServerId(serverId)
        return if (entity?.isDeletedLocally == true) null else entity?.toDomain()
    }

    override suspend fun addCategory(
        arName: String?,
        enName: String?
    ): Result<Unit> {
        return try {
            if (arName.isNullOrBlank() && enName.isNullOrBlank()) {
                return Result.failure(IllegalArgumentException("At least one name (Arabic or English) must be provided for the category."))
            }

            val newCategoryEntity = CategoryEntity(
                serverId = null,
                arName = arName,
                enName = enName,
                isSynced = false,
                lastModified = System.currentTimeMillis(),
                isDeletedLocally = false
            )
            categoryDao.insertCategories(listOf(newCategoryEntity))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCategory(
        category: Category,
        newArName: String?,
        newEnName: String?
    ): Result<Category> {
        return try {
            if (newArName.isNullOrBlank() && newEnName.isNullOrBlank()) {
                return Result.failure(IllegalArgumentException("At least one name (Arabic or English) must be provided for the category."))
            }

            val entityToUpdate = CategoryEntity(
                localId = category.localId,
                serverId = category.serverId,
                arName = newArName,
                enName = newEnName,
                isSynced = false,
                lastModified = System.currentTimeMillis(),
                isDeletedLocally = category.isDeletedLocally
            )
            categoryDao.updateCategory(entityToUpdate)
            Result.success(entityToUpdate.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCategory(category: Category): Result<Unit> {
        return try {
            val entityToDelete = categoryDao.getCategoryByLocalId(category.localId)
                ?: return Result.failure(NoSuchElementException("Category not found for deletion"))

            val categoryToMarkAsDeleted = entityToDelete.copy(
                isDeletedLocally = true,
                isSynced = false,
                lastModified = System.currentTimeMillis()
            )
            categoryDao.updateCategory(categoryToMarkAsDeleted)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncCategories(): Result<Unit> {
        // todo: Implement the API service integration for syncing categories
        println("CategoryRepositoryImpl: syncCategories() called, API service not yet integrated.")
        return Result.success(Unit)
    }
}
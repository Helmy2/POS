package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.features.inventory.data.local.dao.CategoryDao
import com.wael.astimal.pos.features.inventory.data.local.entity.CategoryEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.remote.dto.CategoryDto
import com.wael.astimal.pos.features.inventory.data.remote.dto.toEntity
import com.wael.astimal.pos.features.inventory.domain.entity.Category
import com.wael.astimal.pos.features.inventory.domain.entity.toDto
import com.wael.astimal.pos.features.inventory.domain.repository.CategoryRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
    private val supabaseClient: SupabaseClient,
) : CategoryRepository {


    override fun getCategories(query: String): Flow<List<Category>> {
        return categoryDao.searchCategoriesFlow(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveCategory(
        category: Category
    ): Result<Long> {
        return try {
            val entity = category.toDto()

            val result = if (category.id == Id.new) {
                supabaseClient.from("categories").insert(entity) {
                    select()
                }.decodeSingle<CategoryDto>()
            } else {
                supabaseClient.from("categories").update(entity) {
                    filter {
                        eq("id", entity.id)
                    }
                    select()
                }.decodeSingle<CategoryDto>()
            }

            val localId = categoryDao.insertOrUpdate(
                result.toEntity().copy(localId = category.id.local)
            )

            Result.success(localId)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteCategory(category: Category): Result<Unit> {
        return try {
            supabaseClient.from("categories").delete {
                filter {
                    eq("id", category.id.server!!)
                }
            }
            categoryDao.deleteCategoryByLocalId(category.id.local)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncWithServer(entities: List<CategoryEntity>): Result<Unit> {
        return runCatching {
            val entities = entities.map { dto ->
                val existingEntity = categoryDao.getCategoryByServerId(
                    dto.serverId ?: throw Exception("Server ID not found")
                )
                dto.copy(localId = existingEntity?.localId ?: 0L)
            }
            categoryDao.upsertAll(entities)
        }
    }

    override suspend fun getCategoryByServerId(
        id: Long
    ): Result<Category> {
        return runCatching {
            categoryDao.getCategoryByServerId(id)?.toDomain()
                ?: throw Exception("Category not found")
        }
    }
}
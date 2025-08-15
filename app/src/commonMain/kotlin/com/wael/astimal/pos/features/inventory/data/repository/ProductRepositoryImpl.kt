package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.core.util.deleteRecordAndLog
import com.wael.astimal.pos.features.inventory.data.local.dao.ProductDao
import com.wael.astimal.pos.features.inventory.data.local.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.remote.dto.ProductDto
import com.wael.astimal.pos.features.inventory.data.remote.dto.toEntity
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.toDto
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ProductRepositoryImpl(
    private val productDao: ProductDao,
    private val supabaseClient: SupabaseClient,
) : ProductRepository {

    override fun getProducts(query: String): Flow<List<Product>> {
        return productDao.searchProductsWithDetailsFlow(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun saveProduct(product: Product): Result<Long> {
        return try {
            val entity = product.toDto()

            val result = if (product.id == "") {
                supabaseClient.from("products").insert(
                    entity.copy(id = Uuid.random().toString())
                ) {
                    select()
                }.decodeSingle<ProductDto>()
            } else {
                supabaseClient.from("products").update(entity) {
                    filter {
                        eq("id", entity.id)
                    }
                    select()
                }.decodeSingle<ProductDto>()
            }

            val localId = productDao.insertOrUpdate(result.toEntity())

            Result.success(localId)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(product: Product): Result<Unit> {
        return try {
            supabaseClient.deleteRecordAndLog(
                targetTableName = "products",
                targetRecordId = product.id
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun syncWithServer(productsDto: List<ProductEntity>): Result<Unit> {
        return runCatching {
            productsDto.forEach {
                productDao.insertOrUpdate(it)
            }
        }
    }

    override suspend fun getUnsyncedProducts(): Result<List<Product>> {
        return try {
            Result.success(productDao.getUnsyncedProducts().map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAll(ids: List<String>): Result<Unit> {
        return try {
            ids.forEach { productDao.hardDelete(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
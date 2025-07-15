package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.features.inventory.data.local.dao.ProductDao
import com.wael.astimal.pos.features.inventory.data.local.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.remote.dto.ProductDto
import com.wael.astimal.pos.features.inventory.data.remote.dto.toEntity
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.toDto
import com.wael.astimal.pos.features.inventory.domain.repository.CategoryRepository
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.UnitRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(
    private val productDao: ProductDao,
    private val supabaseClient: SupabaseClient,
    private val categoryRepository: CategoryRepository,
    private val unitRepository: UnitRepository,
) : ProductRepository {

    override fun getProducts(query: String): Flow<List<Product>> {
        return productDao.searchProductsWithDetailsFlow(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getProductByLocalId(localId: Long): Result<Product> {
        return runCatching {
            val entity = productDao.getProductWithDetailsByLocalId(localId)
            if (entity?.product?.isDeletedLocally == true) throw Exception("Product not found")
            entity?.toDomain() ?: throw Exception("Product not found")
        }
    }

    override suspend fun saveProduct(product: Product): Result<Long> {
        return try {
            val entity = product.toDto()

            val result = if (product.id == Id.new) {
                supabaseClient.from("products").insert(entity) {
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

            val localId = productDao.insertOrUpdate(
                result.toEntity(
                    categoryId = result.categoryId?.let { id ->
                        categoryRepository.getCategoryByServerId(
                            id
                        )
                    }?.getOrThrow()?.id?.local,
                    mainUnitId = unitRepository.getUnitByServerId(result.mainUnitId)
                        .getOrThrow().id.local,
                    subUnitId = result.subUnitId?.let { id ->
                        unitRepository.getUnitByServerId(
                            id
                        )
                    }?.getOrThrow()?.id?.local
                ).copy(localId = product.id.local)
            )

            Result.success(localId)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(product: Product): Result<Unit> {
        return try {
            supabaseClient.from("products").delete {
                filter {
                    eq("id", product.id.server!!)
                }
            }
            productDao.deleteProductByLocalId(product.id.local)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncWithServer(productsDto: List<ProductEntity>): Result<Unit> {
        return runCatching {
            productsDto.map { dto ->
                val existingEntity = productDao.getProductByServerId(
                    dto.serverId ?: throw Exception("Server ID not found")
                )
                dto.copy(
                    localId = existingEntity?.product?.localId ?: 0L
                )
            }.forEach {
                productDao.insertOrUpdate(it)
            }
        }
    }

    override suspend fun getProductByServerId(serverId: Long): Result<Product> {
        return runCatching {
            productDao.getProductByServerId(serverId)?.toDomain()
                ?: throw Exception("Product not found")
        }
    }

    override suspend fun getUnsyncedProducts(): Result<List<Product>> {
        return try {
            Result.success(productDao.getUnsyncedProducts().map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
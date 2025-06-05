package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.features.inventory.data.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.local.dao.ProductDao
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(
    private val productDao: ProductDao,
) : ProductRepository {

    override fun getProducts(query: String): Flow<List<Product>> {
        return productDao.searchProductsWithDetailsFlow(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getProductByLocalIdFlow(localId: Long): Flow<Product?> {
        return productDao.getProductWithDetailsByLocalIdFlow(localId).map { entityWithDetails ->
            entityWithDetails?.takeUnless { it.product.isDeletedLocally }?.toDomain()
        }
    }


    override suspend fun getProductByLocalId(localId: Long): Product? {
        val entity = productDao.getProductWithDetailsByLocalId(localId)
        return if (entity?.product?.isDeletedLocally == true) null else entity?.toDomain()
    }

    override suspend fun getProductByServerId(serverId: Int): Product? {
        val entity = productDao.getProductWithDetailsByServerId(serverId)
        return if (entity?.product?.isDeletedLocally == true) null else entity?.toDomain()
    }

    override suspend fun addProduct(productEntity: ProductEntity): Result<Unit> {
        return try {
            if (productEntity.arName.isNullOrBlank() && productEntity.enName.isNullOrBlank()) {
                return Result.failure(IllegalArgumentException("At least one name (Arabic or English) must be provided for the product."))
            }
            // Ensure it's marked as new and unsynced
            val entityToInsert = productEntity.copy(
                localId = 0L, // Let Room auto-generate
                serverId = null,
                isSynced = false,
                lastModified = System.currentTimeMillis(),
                isDeletedLocally = false
            )
            productDao.insertProducts(listOf(entityToInsert))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProduct(productEntity: ProductEntity): Result<Product> {
        return try {
            if (productEntity.arName.isNullOrBlank() && productEntity.enName.isNullOrBlank()) {
                return Result.failure(IllegalArgumentException("At least one name (Arabic or English) must be provided for the product."))
            }
            val entityToUpdate = productEntity.copy(
                isSynced = false,
                lastModified = System.currentTimeMillis()
            )
            productDao.updateProduct(entityToUpdate) // Assumes localId is correctly set in productEntity
            val updatedProductWithDetails = productDao.getProductWithDetailsByLocalId(entityToUpdate.localId)
                ?: return Result.failure(IllegalStateException("Failed to retrieve product after update"))

            Result.success(updatedProductWithDetails.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(product: Product): Result<Unit> {
        return try {
            val productEntityFromDb = productDao.getProductByLocalId(product.localId)
                ?: return Result.failure(NoSuchElementException("Product entity not found for deletion with localId: ${product.localId}"))


            val productToMarkAsDeleted = productEntityFromDb.copy(
                isDeletedLocally = true,
                isSynced = false,
                lastModified = System.currentTimeMillis()
            )
            productDao.updateProduct(productToMarkAsDeleted)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
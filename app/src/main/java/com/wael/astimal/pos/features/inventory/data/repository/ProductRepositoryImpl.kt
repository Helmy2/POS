package com.wael.astimal.pos.features.inventory.data.repository

import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.inventory.data.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.local.dao.ProductDao
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val productDao: ProductDao,
    private val stockRepository: StockRepository,
    private val sessionManager: SessionManager
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
            if (productEntity.arName.isBlank() && productEntity.enName.isBlank()) {
                return Result.failure(IllegalArgumentException("At least one name (Arabic or English) must be provided for the product."))
            }

            appDatabase.withTransaction {
                val entityToInsert = productEntity.copy(
                    localId = 0L,
                    serverId = null,
                    isSynced = false,
                    updatedAt = System.currentTimeMillis(),
                    isDeletedLocally = false
                )
                val newProductId = productDao.insertOrUpdate(entityToInsert)

                val openingBalance = productEntity.openingBalanceQuantity
                val currentUser = sessionManager.getCurrentUser().first()
                val fullProduct = productDao.getProductWithDetailsByLocalId(newProductId)?.toDomain()

                if (openingBalance != null && openingBalance > 0 && currentUser != null && fullProduct?.store != null) {
                    val adjustment = StockAdjustmentEntity(
                        serverId = null,
                        storeId = fullProduct.store.id.local,
                        productId = newProductId,
                        userId = currentUser.id,
                        reason = StockAdjustmentReason.INITIAL_COUNT,
                        notes = "Opening Balance",
                        quantityChange = openingBalance,
                    )
                    stockRepository.addStockAdjustment(adjustment)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProduct(productEntity: ProductEntity): Result<Product> {
        return try {
            if (productEntity.arName.isBlank() && productEntity.enName.isBlank()) {
                return Result.failure(IllegalArgumentException("At least one name (Arabic or English) must be provided for the product."))
            }

            appDatabase.withTransaction {
                val oldProductEntity = productDao.getProductByLocalId(productEntity.localId)
                    ?: throw NoSuchElementException("Product not found for update with localId: ${productEntity.localId}")

                val entityToUpdate = productEntity.copy(
                    isSynced = false, updatedAt = System.currentTimeMillis()
                )
                productDao.updateProduct(entityToUpdate)

                val openingBalanceDifference = (productEntity.openingBalanceQuantity
                    ?: 0.0) - (oldProductEntity.openingBalanceQuantity ?: 0.0)

                if (openingBalanceDifference != 0.0) {
                    val currentUser = sessionManager.getCurrentUser().first()
                        ?: throw Exception("User not authenticated for stock adjustment.")
                    val fullProduct = getProductByLocalId(productEntity.localId) ?: throw Exception(
                        "Could not retrieve full product details for adjustment."
                    )
                    val store = fullProduct.store

                    val adjustment = StockAdjustmentEntity(
                        localId = 0L,
                        serverId = null,
                        storeId = store.id.local,
                        productId = fullProduct.id.local,
                        userId = currentUser.id,
                        reason = StockAdjustmentReason.RECOUNT,
                        notes = "Opening balance updated.",
                        quantityChange = openingBalanceDifference,
                        updatedAt = System.currentTimeMillis(),
                        isSynced = false
                    )
                    stockRepository.addStockAdjustment(adjustment)
                }
            }

            val updatedProductWithDetails =
                productDao.getProductWithDetailsByLocalId(productEntity.localId)
                    ?: return Result.failure(IllegalStateException("Failed to retrieve product after update"))

            Result.success(updatedProductWithDetails.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(productLocalId: Long): Result<Unit> {
        return try {
            appDatabase.withTransaction {
                val currentUser = sessionManager.getCurrentUser().first()
                    ?: throw Exception("User not authenticated for delete operation")

                val productToDelete = getProductByLocalId(productLocalId)
                    ?: throw NoSuchElementException("Product not found with localId: $productLocalId")

                // Fetch all stock entries for this product to zero them out
                val allStocks =
                    stockRepository.getStoreStocks(query = "", selectedStoreId = null).first()
                val productStocks = allStocks.filter { it.product.id.local == productLocalId }

                for (stockItem in productStocks) {
                    if (stockItem.quantity != 0.0) {
                        val adjustment = StockAdjustmentEntity(
                            localId = 0L,
                            serverId = null,
                            storeId = stockItem.store.id.local,
                            productId = stockItem.product.id.local,
                            userId = currentUser.id,
                            reason = StockAdjustmentReason.OTHER,
                            notes = "Product ${productToDelete.localizedName.arName} deleted.",
                            quantityChange = -stockItem.quantity, // Reverse the quantity
                            updatedAt = System.currentTimeMillis(),
                            isSynced = false
                        )
                        stockRepository.addStockAdjustment(adjustment)
                    }
                }

                // Mark the product as deleted
                val productEntityFromDb = productDao.getProductByLocalId(productLocalId)
                    ?: throw NoSuchElementException("Product entity not found for deletion with localId: $productLocalId")

                val productToMarkAsDeleted = productEntityFromDb.copy(
                    isDeletedLocally = true,
                    isSynced = false,
                    updatedAt = System.currentTimeMillis()
                )
                productDao.updateProduct(productToMarkAsDeleted)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
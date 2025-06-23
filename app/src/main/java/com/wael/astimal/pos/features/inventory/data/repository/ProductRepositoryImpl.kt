package com.wael.astimal.pos.features.inventory.data.repository

import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.data.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.local.dao.ProductDao
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val productDao: ProductDao,
    private val stockRepository: StockRepository,
    private val userRepository: UserRepository
) : ProductRepository {

    override fun getProducts(query: String): Flow<List<Product>> {
        return productDao.searchProductsWithDetailsFlow(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }


    override suspend fun getProductByLocalId(localId: Long): Product? {
        val entity = productDao.getProductWithDetailsByLocalId(localId)
        return if (entity?.product?.isDeletedLocally == true) null else entity?.toDomain()
    }

    override suspend fun saveProduct(productEntity: ProductEntity): Result<Unit> {
        return runCatching {
            if (productEntity.arName.isBlank() && productEntity.enName.isBlank()) {
                return Result.failure(IllegalArgumentException("At least one name (Arabic or English) must be provided for the product."))
            }

            appDatabase.withTransaction {
                if (productEntity.localId == 0L) {
                    val newProductId = productDao.insertOrUpdate(productEntity)

                    val openingBalance = productEntity.openingBalanceQuantity
                    val currentUser = userRepository.getCurrentUser()
                    val fullProduct =
                        productDao.getProductWithDetailsByLocalId(newProductId)?.toDomain()

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
                } else {
                    val oldProductEntity = productDao.getProductByLocalId(productEntity.localId)
                        ?: throw NoSuchElementException("Product not found for update with localId: ${productEntity.localId}")

                    productDao.insertOrUpdate(productEntity)

                    val openingBalanceDifference = (productEntity.openingBalanceQuantity
                        ?: 0.0) - (oldProductEntity.openingBalanceQuantity ?: 0.0)

                    if (openingBalanceDifference != 0.0) {
                        val currentUser = userRepository.getCurrentUser()
                            ?: throw Exception("User not authenticated for stock adjustment.")
                        val fullProduct =
                            getProductByLocalId(productEntity.localId) ?: throw Exception(
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
                            updatedAt = Clock.now(),
                            isSynced = false
                        )
                        stockRepository.addStockAdjustment(adjustment)
                    }
                }
            }
        }
    }

    override suspend fun deleteProduct(productLocalId: Long): Result<Unit> {
        return try {
            appDatabase.withTransaction {
                val currentUser = userRepository.getCurrentUser()
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
                            updatedAt = Clock.now(),
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
                    updatedAt = Clock.now()
                )
                productDao.insertOrUpdate(productToMarkAsDeleted)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
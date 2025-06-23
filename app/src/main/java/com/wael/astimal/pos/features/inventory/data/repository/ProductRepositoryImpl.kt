package com.wael.astimal.pos.features.inventory.data.repository

import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.data.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.local.dao.ProductDao
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import com.wael.astimal.pos.features.inventory.domain.entity.toEntity
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val productDao: ProductDao,
    private val stockRepository: StockRepository,
    private val userRepository: UserRepository
) : ProductRepository {

    override fun getProducts(query: String): Flow<Result<List<Product>>> {
        return productDao.searchProductsWithDetailsFlow(query).map { entities ->
            runCatching { entities.map { it.toDomain() } }
        }.catch { emit(Result.failure(it)) }
    }


    override suspend fun getProductByLocalId(localId: Long): Result<Product> {
        return runCatching {
            val entity = productDao.getProductWithDetailsByLocalId(localId)
            if (entity?.product?.isDeletedLocally == true)
                throw Exception("Product not found")
            entity?.toDomain() ?: throw Exception("Product not found")
        }
    }

    override suspend fun saveProduct(product: Product): Result<Unit> {
        return runCatching {
            appDatabase.withTransaction {
                if (product.id.local == 0L) {
                    val newProductId = productDao.insertOrUpdate(product.toEntity())

                    val openingBalance = product.openingBalanceQuantity
                    val currentUser = userRepository.getCurrentUser()
                    val fullProduct =
                        productDao.getProductWithDetailsByLocalId(newProductId)?.toDomain()

                    if (openingBalance > 0 && currentUser != null && fullProduct?.store != null) {
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
                    val oldProductEntity = productDao.getProductByLocalId(product.id.local)
                        ?: throw NoSuchElementException("Product not found for update with localId: ${product.id.local}")

                    productDao.insertOrUpdate(product.toEntity())

                    val openingBalanceDifference =
                        product.openingBalanceQuantity - oldProductEntity.openingBalanceQuantity

                    if (openingBalanceDifference != 0.0) {
                        val currentUser = userRepository.getCurrentUser()
                            ?: throw Exception("User not authenticated for stock adjustment.")
                        val store = product.store

                        val adjustment = StockAdjustmentEntity(
                            localId = 0L,
                            serverId = null,
                            storeId = store.id.local,
                            productId = product.id.local,
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

    override suspend fun deleteProduct(product: Product): Result<Unit> {
        return try {
            appDatabase.withTransaction {
                val currentUser = userRepository.getCurrentUser()
                    ?: throw Exception("User not authenticated for delete operation")


                // Fetch all stock entries for this product to zero them out
                val allStocks =
                    stockRepository.getStoreStocks(query = "", selectedStoreId = null).first()
                val productStocks = allStocks.filter { it.product.id.local == product.id.local }

                for (stockItem in productStocks) {
                    if (stockItem.quantity != 0.0) {
                        val adjustment = StockAdjustmentEntity(
                            localId = 0L,
                            serverId = null,
                            storeId = stockItem.store.id.local,
                            productId = stockItem.product.id.local,
                            userId = currentUser.id,
                            reason = StockAdjustmentReason.OTHER,
                            notes = "Product ${product.name.arName} deleted.",
                            quantityChange = -stockItem.quantity,
                            updatedAt = Clock.now(),
                            isSynced = false
                        )
                        stockRepository.addStockAdjustment(adjustment)
                    }
                }

                val productToMarkAsDeleted = product.toEntity().copy(
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
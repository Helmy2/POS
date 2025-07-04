package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.data.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.local.dao.ProductDao
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustment
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import com.wael.astimal.pos.features.inventory.domain.entity.toEntity
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(
    private val productDao: ProductDao,
    private val stockRepository: StockRepository,
    private val userRepository: UserRepository,
    private val storeRepository: StoreRepository,
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

    override suspend fun saveProduct(product: Product): Result<Unit> {
        return runCatching {
            saveProduct(product.toEntity()).getOrThrow()
        }
    }

    private suspend fun saveProduct(product: ProductEntity): Result<Unit> {
        return runCatching {
            if (product.localId == 0L) {
                val newProductId = productDao.insertOrUpdate(product)

                val openingBalance = product.openingBalanceQuantity
                val currentUser = userRepository.getCurrentUser()
                val fullProduct =
                    productDao.getProductWithDetailsByLocalId(newProductId)?.toDomain()

                if (openingBalance > 0 && currentUser != null && fullProduct?.store != null) {
                    val adjustment = StockAdjustment(
                        id = Id.new,
                        store = fullProduct.store,
                        product = fullProduct,
                        user = currentUser,
                        reason = StockAdjustmentReason.INITIAL_COUNT,
                        notes = "Opening Balance",
                        quantityChange = openingBalance,
                        createdAt = Clock.now()
                    )
                    stockRepository.addStockAdjustment(adjustment)
                }
            } else {
                val oldProductEntity = productDao.getProductByLocalId(product.localId)
                    ?: throw NoSuchElementException("Product not found for update with localId: ${product.localId}")

                productDao.insertOrUpdate(product)

                val openingBalanceDifference =
                    product.openingBalanceQuantity - oldProductEntity.openingBalanceQuantity

                if (openingBalanceDifference != 0.0) {
                    val currentUser = userRepository.getCurrentUser()
                        ?: throw Exception("User not authenticated for stock adjustment.")

                    val store =
                        product.storeId?.let { storeRepository.getStoreByLocalId(Id(local = it)) }
                        ?.getOrThrow() ?: throw Exception("Store not found")
                    val product = getProductByLocalId(product.localId).getOrThrow()


                    val adjustment = StockAdjustment(
                        id = Id.new,
                        store = store,
                        product = product,
                        user = currentUser,
                        reason = StockAdjustmentReason.RECOUNT,
                        notes = "Opening balance updated.",
                        quantityChange = openingBalanceDifference,
                        createdAt = Clock.now(),
                    )
                    stockRepository.addStockAdjustment(adjustment)
                }
            }
        }
    }

    override suspend fun deleteProduct(product: Product): Result<Unit> {
        return try {
            val currentUser = userRepository.getCurrentUser()
                ?: throw Exception("User not authenticated for delete operation")


            // Fetch all stock entries for this product to zero them out
            val allStocks =
                stockRepository.getStoreStocks(query = "", selectedStoreId = null).first()
            val productStocks = allStocks.filter { it.product.id.local == product.id.local }

            for (stockItem in productStocks) {
                if (stockItem.quantity != 0.0) {
                    val adjustment = StockAdjustment(
                        id = Id.new,
                        store = stockItem.store,
                        product = stockItem.product,
                        user = currentUser,
                        reason = StockAdjustmentReason.OTHER,
                        notes = "Product ${product.name.arName} deleted.",
                        quantityChange = -stockItem.quantity,
                        createdAt = Clock.now(),
                    )
                    stockRepository.addStockAdjustment(adjustment)
                }
            }

            val productToMarkAsDeleted = product.toEntity().copy(
                isDeletedLocally = true, isSynced = false, updatedAt = Clock.now()
            )
            productDao.insertOrUpdate(productToMarkAsDeleted)

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
                saveProduct(it)
            }
        }
    }

    override suspend fun getProductByServerId(serverId: Long): Result<Product> {
        return runCatching {
            productDao.getProductByServerId(serverId)?.toDomain()
                ?: throw Exception("Product not found")
        }
    }
}
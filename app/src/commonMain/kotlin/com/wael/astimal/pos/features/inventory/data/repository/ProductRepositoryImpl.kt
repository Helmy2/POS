package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.data.local.dao.ProductDao
import com.wael.astimal.pos.features.inventory.data.local.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.toDomain
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustment
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import com.wael.astimal.pos.features.inventory.domain.entity.toEntity
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(
    private val productDao: ProductDao,
    private val stockRepository: StockRepository,
    private val userRepository: UserRepository,
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
            productDao.insertOrUpdate(product)
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
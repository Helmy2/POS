package com.wael.astimal.pos.features.inventory.domain.repository

import com.wael.astimal.pos.features.inventory.data.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(query: String): Flow<List<Product>>
    fun getProductByLocalIdFlow(localId: Long): Flow<Product?>
    suspend fun getProductByLocalId(localId: Long): Product?
    suspend fun getProductByServerId(serverId: Int): Product?
    suspend fun addProduct(productEntity: ProductEntity): Result<Unit>
    suspend fun updateProduct(productEntity: ProductEntity): Result<Product>
    suspend fun deleteProduct(product: Product): Result<Unit>
}
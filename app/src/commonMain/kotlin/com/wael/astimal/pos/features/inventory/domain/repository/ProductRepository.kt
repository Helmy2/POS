package com.wael.astimal.pos.features.inventory.domain.repository

import com.wael.astimal.pos.features.inventory.data.local.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(query: String = ""): Flow<List<Product>>
    suspend fun saveProduct(product: Product): Result<Long>
    suspend fun deleteProduct(product: Product): Result<Unit>
    suspend fun syncWithServer(productsDto: List<ProductEntity>): Result<Unit>
    suspend fun getUnsyncedProducts(): Result<List<Product>>
    suspend fun deleteAll(ids: List<String>): Result<Unit>
}
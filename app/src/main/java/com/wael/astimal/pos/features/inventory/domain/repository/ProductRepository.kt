package com.wael.astimal.pos.features.inventory.domain.repository

import com.wael.astimal.pos.features.inventory.data.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(query: String = ""): Flow<List<Product>>
    suspend fun getProductByLocalId(localId: Long): Product?
    suspend fun saveProduct(productEntity: ProductEntity): Result<Unit>
    suspend fun deleteProduct(productLocalId: Long): Result<Unit>
}
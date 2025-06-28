package com.wael.astimal.pos.features.inventory.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wael.astimal.pos.features.inventory.data.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.entity.ProductWithDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(product: ProductEntity): Long

    @Transaction
    @Query("SELECT * FROM products WHERE localId = :localId LIMIT 1")
    suspend fun getProductWithDetailsByLocalId(localId: Long): ProductWithDetailsEntity?

    @Query("SELECT * FROM products WHERE localId = :localId LIMIT 1")
    suspend fun getProductByLocalId(localId: Long): ProductEntity?

    @Transaction
    @Query("""
        SELECT * FROM products 
        WHERE NOT isDeletedLocally 
        AND (arName LIKE '%' || :query || '%' OR enName LIKE '%' || :query || '%')
        ORDER BY enName ASC, arName ASC
    """)
    fun searchProductsWithDetailsFlow(query: String): Flow<List<ProductWithDetailsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(products: List<ProductEntity>)

    @Query("SELECT * FROM products WHERE serverId = :serverId LIMIT 1")
    suspend fun getProductByServerId(serverId: Long): ProductEntity?

}
package com.wael.astimal.pos.features.inventory.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.wael.astimal.pos.features.inventory.data.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.entity.ProductWithDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE localId = :localId")
    suspend fun deleteProductByLocalId(localId: Long)

    @Transaction
    @Query("SELECT * FROM products WHERE localId = :localId LIMIT 1")
    fun getProductWithDetailsByLocalIdFlow(localId: Long): Flow<ProductWithDetailsEntity?>

    @Transaction
    @Query("SELECT * FROM products WHERE localId = :localId LIMIT 1")
    suspend fun getProductWithDetailsByLocalId(localId: Long): ProductWithDetailsEntity?

    @Query("SELECT * FROM products WHERE localId = :localId LIMIT 1")
    suspend fun getProductByLocalId(localId: Long): ProductEntity?

    @Transaction
    @Query("SELECT * FROM products WHERE serverId = :serverId LIMIT 1")
    suspend fun getProductWithDetailsByServerId(serverId: Int): ProductWithDetailsEntity?

    @Transaction
    @Query("SELECT * FROM products WHERE NOT isDeletedLocally ORDER BY enName ASC, arName ASC")
    fun getAllProductsWithDetailsFlow(): Flow<List<ProductWithDetailsEntity>>

    @Transaction
    @Query("""
        SELECT * FROM products 
        WHERE NOT isDeletedLocally 
        AND (arName LIKE '%' || :query || '%' OR enName LIKE '%' || :query || '%')
        ORDER BY enName ASC, arName ASC
    """)
    fun searchProductsWithDetailsFlow(query: String): Flow<List<ProductWithDetailsEntity>>

    @Query("DELETE FROM products WHERE localId IN (:localIds)")
    suspend fun deleteProductsByLocalIds(localIds: List<Long>)
}
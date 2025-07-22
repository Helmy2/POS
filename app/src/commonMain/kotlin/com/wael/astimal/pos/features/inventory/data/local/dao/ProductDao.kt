package com.wael.astimal.pos.features.inventory.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wael.astimal.pos.features.inventory.data.local.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.ProductWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(product: ProductEntity): Long

    @Transaction
    @Query("SELECT * FROM products WHERE localId = :localId LIMIT 1")
    suspend fun getProductWithDetailsByLocalId(localId: String): ProductWithDetails?

    @Query("SELECT * FROM products WHERE localId = :localId LIMIT 1")
    suspend fun getProductByLocalId(localId: String): ProductEntity?

    @Query("SELECT averagePurchasePrice FROM products WHERE localId = :localId LIMIT 1")
    suspend fun getAverageCost(localId: String): Double

    @Transaction
    @Query(
        """
        SELECT * FROM products 
        WHERE NOT isDeletedLocally 
        AND (arName LIKE '%' || :query || '%' OR enName LIKE '%' || :query || '%')
        ORDER BY enName ASC, arName ASC
    """
    )
    fun searchProductsWithDetailsFlow(query: String): Flow<List<ProductWithDetails>>

    @Query("SELECT * FROM products WHERE localId = :id LIMIT 1")
    suspend fun getProductById(id: String): ProductWithDetails?

    @Query("DELETE FROM products WHERE localId = :localId")
    suspend fun deleteProductById(localId: String)

    @Query("UPDATE products SET  isSynced = 0, averagePurchasePrice = :newCost WHERE localId = :id")
    suspend fun updateAverageCost(id: String, newCost: Double)

    @Transaction
    @Query("SELECT * FROM products WHERE isSynced = 0")
    suspend fun getUnsyncedProducts(): List<ProductWithDetails>

    @Query("UPDATE products SET isDeletedLocally = 1")
    suspend fun deleteAll()
}
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

    @Query("SELECT averagePurchasePrice FROM products WHERE NOT isDeletedLocally AND localId = :localId LIMIT 1")
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

    @Query("DELETE FROM products WHERE localId = :id")
    suspend fun hardDelete(id: String)

    @Query("UPDATE products SET  isSynced = 0, averagePurchasePrice = :newCost WHERE localId = :id")
    suspend fun updateAverageCost(id: String, newCost: Double)

    @Transaction
    @Query("SELECT * FROM products WHERE NOT isDeletedLocally AND isSynced = 0")
    suspend fun getUnsyncedProducts(): List<ProductWithDetails>
}
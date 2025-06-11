package com.wael.astimal.pos.features.inventory.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wael.astimal.pos.features.inventory.data.entity.StoreProductStockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreProductStockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStock(stock: StoreProductStockEntity)

    @Query("SELECT quantity FROM store_product_stock WHERE storeLocalId = :storeId AND productLocalId = :productId")
    suspend fun getQuantity(storeId: Long, productId: Long): Double?

    @Query("UPDATE store_product_stock SET quantity = quantity + :changeInBaseQuantity WHERE storeLocalId = :storeId AND productLocalId = :productId")
    suspend fun adjustQuantity(storeId: Long, productId: Long, changeInBaseQuantity: Double)

    @Query("SELECT quantity FROM store_product_stock WHERE storeLocalId = :storeId AND productLocalId = :productId")
    fun getQuantityFlow(storeId: Long, productId: Long): Flow<Double?>
}
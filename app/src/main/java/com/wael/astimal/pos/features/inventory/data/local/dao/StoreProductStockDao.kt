package com.wael.astimal.pos.features.inventory.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wael.astimal.pos.features.inventory.data.entity.StoreProductStockEntity
import com.wael.astimal.pos.features.inventory.data.entity.StoreStockWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreProductStockDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStock(stock: StoreProductStockEntity)

    @Query("SELECT quantity FROM store_product_stock WHERE storeLocalId = :storeId AND productLocalId = :productId")
    fun getStockQuantity(storeId: Long, productId: Long): Flow<Double?>

    @Query("UPDATE store_product_stock SET quantity = quantity + :changeInBaseQuantity WHERE storeLocalId = :storeId AND productLocalId = :productId")
    suspend fun adjustQuantity(storeId: Long, productId: Long, changeInBaseQuantity: Double)

    @Query("SELECT * FROM store_product_stock WHERE storeLocalId = :storeId AND productLocalId = :productId")
    fun getStockByStoreAndProduct(storeId: Long, productId: Long): Flow<StoreProductStockEntity?>

    @Transaction
    @Query("SELECT * FROM store_product_stock")
    fun getStoreStocks(): Flow<List<StoreStockWithDetails>>
}
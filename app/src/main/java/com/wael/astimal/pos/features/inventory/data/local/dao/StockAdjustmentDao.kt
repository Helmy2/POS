package com.wael.astimal.pos.features.inventory.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wael.astimal.pos.features.inventory.data.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.data.entity.StockAdjustmentWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface StockAdjustmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(adjustment: StockAdjustmentEntity): Long

    @Transaction
    @Query("SELECT * FROM stock_adjustments WHERE storeId = :storeId AND productId = :productId")
    fun getAdjustmentHistory(storeId: Long, productId: Long): Flow<List<StockAdjustmentWithDetails>>

    @Transaction
    @Query("SELECT * FROM stock_adjustments")
    fun getAllAdjustments(): Flow<List<StockAdjustmentWithDetails>>
}
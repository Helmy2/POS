package com.wael.astimal.pos.features.inventory.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wael.astimal.pos.features.inventory.data.local.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.StockAdjustmentWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface StockAdjustmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(adjustment: StockAdjustmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(adjustments: List<StockAdjustmentEntity>)

    @Query("SELECT * FROM stock_adjustments WHERE serverId = :serverId")
    suspend fun getAdjustmentByServerId(serverId: String): StockAdjustmentEntity?

    @Query("SELECT sum(quantityChange) FROM stock_adjustments WHERE storeId = :storeId AND productId = :productId")
    fun getStockQuantity(storeId: Long, productId: Long): Flow<Double?>

    @Query("SELECT * FROM stock_adjustments")
    fun getAll(): Flow<List<StockAdjustmentWithDetails>>

    @Query("UPDATE stock_adjustments SET isDeletedLocally = 1 WHERE localId = :localId")
    suspend fun softDeleteByLocalId(localId: Long)

    @Query("DELETE FROM stock_adjustments WHERE serverId = :id")
    suspend fun deleteByServerId(id: String)

    @Query("SELECT * FROM stock_adjustments WHERE NOT isSynced")
    suspend fun getAllUnSynced(): List<StockAdjustmentWithDetails>

    @Query("SELECT * FROM stock_adjustments WHERE isDeletedLocally = 1")
    suspend fun getAllDeleted(): List<StockAdjustmentWithDetails>

    @Query("DELETE FROM stock_adjustments WHERE invoiceId = :orderId")
    fun deleteAdjustmentsByInvoiceId(orderId: String)
}
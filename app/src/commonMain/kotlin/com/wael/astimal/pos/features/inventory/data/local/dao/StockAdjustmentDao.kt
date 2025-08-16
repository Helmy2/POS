package com.wael.astimal.pos.features.inventory.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wael.astimal.pos.features.inventory.data.local.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.StockAdjustmentWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface StockAdjustmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(adjustment: StockAdjustmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(adjustments: List<StockAdjustmentEntity>)

    @Query("SELECT sum(quantityChange) FROM stock_adjustments WHERE NOT isDeletedLocally AND storeId = :storeId AND productId = :productId")
    fun getStockQuantity(storeId: String, productId: String): Flow<Double?>

    @Query("SELECT sum(quantityChange) FROM stock_adjustments WHERE NOT isDeletedLocally AND productId = :productId")
    suspend fun getStockTotalQuantity(productId: String): Double?

    @Transaction
    @Query("SELECT * FROM stock_adjustments WHERE NOT isDeletedLocally")
    fun getAll(): Flow<List<StockAdjustmentWithDetails>>


    @Query("DELETE FROM stock_adjustments WHERE localId = :id")
    suspend fun hardDelete(id: String)

    @Transaction
    @Query("SELECT * FROM stock_adjustments WHERE NOT isDeletedLocally AND NOT isSynced")
    suspend fun getAllUnSynced(): List<StockAdjustmentWithDetails>


    @Query("SELECT sum(quantityChange) FROM stock_adjustments WHERE NOT isDeletedLocally AND storeId IN (:storesId) AND productId = :productId")
    suspend fun getStockInStores(storesId: List<String>, productId: String): Double?

    @Query("SELECT * FROM stock_adjustments WHERE transactionId = :transferId")
    suspend fun getAdjustmentsByTransferId(transferId: String): List<StockAdjustmentEntity>

    @Query("SELECT * FROM stock_adjustments WHERE invoiceId = :invoiceId")
    suspend fun getAdjustmentsByInvoiceId(invoiceId: String): List<StockAdjustmentEntity>

}
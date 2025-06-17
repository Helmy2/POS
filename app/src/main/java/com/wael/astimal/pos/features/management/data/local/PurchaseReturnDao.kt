package com.wael.astimal.pos.features.management.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.wael.astimal.pos.features.management.data.entity.PurchaseReturnEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseReturnProductEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseReturnWithDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseReturnDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePurchaseReturn(purchaseReturn: PurchaseReturnEntity): Long

    @Update
    suspend fun updatePurchaseReturn(purchaseReturn: PurchaseReturnEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseReturnItems(items: List<PurchaseReturnProductEntity>)

    @Query("DELETE FROM purchase_return_products WHERE purchaseReturnLocalId = :returnId")
    suspend fun deleteItemsForPurchaseReturn(returnId: Long)

    @Query("SELECT * FROM purchase_returns WHERE localId = :localId")
    suspend fun getPurchaseReturnEntityByLocalId(localId: Long): PurchaseReturnEntity?

    @Transaction
    @Query("SELECT * FROM purchase_returns WHERE localId = :localId")
    suspend fun getPurchaseReturnWithDetails(localId: Long): PurchaseReturnWithDetailsEntity?

    @Transaction
    suspend fun insertPurchaseReturnWithItems(purchaseReturn: PurchaseReturnEntity, items: List<PurchaseReturnProductEntity>): Long {
        val returnId = insertOrUpdatePurchaseReturn(purchaseReturn)
        val itemsWithId = items.map { it.copy(purchaseReturnLocalId = returnId) }
        if (itemsWithId.isNotEmpty()) {
            insertPurchaseReturnItems(itemsWithId)
        }
        return returnId
    }

    @Transaction
    suspend fun updatePurchaseReturnWithItems(purchaseReturn: PurchaseReturnEntity, items: List<PurchaseReturnProductEntity>) {
        updatePurchaseReturn(purchaseReturn)
        deleteItemsForPurchaseReturn(purchaseReturn.localId)
        val itemsWithId = items.map { it.copy(purchaseReturnLocalId = purchaseReturn.localId) }
        if (itemsWithId.isNotEmpty()) {
            insertPurchaseReturnItems(itemsWithId)
        }
    }

    @Transaction
    @Query("SELECT * FROM purchase_returns WHERE NOT isDeletedLocally")
    fun getAllPurchaseReturnsWithDetailsFlow(): Flow<List<PurchaseReturnWithDetailsEntity>>

    @Transaction
    @Query("SELECT * FROM purchase_returns WHERE supplierLocalId = :supplierId AND NOT isDeletedLocally")
    fun getReturnsBySupplierId(supplierId: Long): Flow<List<PurchaseReturnWithDetailsEntity>>

    @Query("SELECT MAX(invoiceNumber) FROM purchase_returns WHERE invoiceNumber LIKE :pattern")
    suspend fun getLastInvoiceNumber(pattern: String): String?
}
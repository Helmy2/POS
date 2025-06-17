package com.wael.astimal.pos.features.management.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.wael.astimal.pos.features.management.data.entity.PurchaseEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseProductEntity
import com.wael.astimal.pos.features.management.data.entity.PurchaseWithDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePurchase(purchase: PurchaseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<PurchaseProductEntity>)

    @Update
    suspend fun updatePurchase(purchase: PurchaseEntity)

    @Query("DELETE FROM purchase_products WHERE purchaseLocalId = :purchaseId")
    suspend fun deleteItemsForPurchase(purchaseId: Long)

    @Transaction
    @Query("SELECT * FROM purchases WHERE NOT isDeletedLocally")
    fun getAllPurchasesWithDetailsFlow(): Flow<List<PurchaseWithDetailsEntity>>

    @Transaction
    @Query("SELECT * FROM purchases WHERE supplierLocalId = :supplierId AND NOT isDeletedLocally")
    fun getPurchasesBySupplierId(supplierId: Long): Flow<List<PurchaseWithDetailsEntity>>

    @Transaction
    @Query("SELECT * FROM purchases WHERE localId = :localId")
    suspend fun getPurchaseWithDetails(localId: Long): PurchaseWithDetailsEntity?

    @Transaction
    suspend fun insertPurchaseWithItems(purchase: PurchaseEntity, items: List<PurchaseProductEntity>): Long {
        val purchaseId = insertOrUpdatePurchase(purchase)
        val itemsWithId = items.map { it.copy(purchaseLocalId = purchaseId) }
        if (itemsWithId.isNotEmpty()) {
            insertPurchaseItems(itemsWithId)
        }
        return purchaseId
    }

    @Transaction
    suspend fun updatePurchaseWithItems(purchase: PurchaseEntity, items: List<PurchaseProductEntity>) {
        updatePurchase(purchase)
        deleteItemsForPurchase(purchase.localId)
        val itemsWithId = items.map { it.copy(purchaseLocalId = purchase.localId) }
        if (itemsWithId.isNotEmpty()) {
            insertPurchaseItems(itemsWithId)
        }
    }

    @Query("SELECT MAX(invoiceNumber) FROM purchases WHERE invoiceNumber LIKE :pattern")
    suspend fun getLastInvoiceNumber(pattern: String): String?
}
package com.wael.astimal.pos.features.management.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @androidx.room.Transaction
    @Query("SELECT * FROM purchases WHERE NOT isDeletedLocally ORDER BY purchaseDate DESC")
    fun getAllPurchasesWithDetailsFlow(): Flow<List<PurchaseWithDetailsEntity>>

    @androidx.room.Transaction
    suspend fun insertPurchaseWithItems(purchase: PurchaseEntity, items: List<PurchaseProductEntity>): Long {
        val purchaseId = insertOrUpdatePurchase(purchase)
        val itemsWithId = items.map { it.copy(purchaseLocalId = purchaseId) }
        insertPurchaseItems(itemsWithId)
        return purchaseId
    }

    @androidx.room.Transaction
    suspend fun updatePurchaseWithItems(purchase: PurchaseEntity, items: List<PurchaseProductEntity>) {
        updatePurchase(purchase)
        deleteItemsForPurchase(purchase.localId)
        val itemsWithId = items.map { it.copy(purchaseLocalId = purchase.localId) }
        insertPurchaseItems(itemsWithId)
    }

    @Query("SELECT * FROM purchases WHERE localId = :localId")
    suspend fun getPurchaseEntityByLocalId(localId: Long): PurchaseEntity?
}
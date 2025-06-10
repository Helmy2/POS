package com.wael.astimal.pos.features.client_management.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wael.astimal.pos.features.client_management.data.entity.PurchaseReturnEntity
import com.wael.astimal.pos.features.client_management.data.entity.PurchaseReturnProductEntity
import com.wael.astimal.pos.features.client_management.data.entity.PurchaseReturnWithDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseReturnDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePurchaseReturn(purchaseReturn: PurchaseReturnEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseReturnItems(items: List<PurchaseReturnProductEntity>)

    @Update
    suspend fun updatePurchaseReturn(purchaseReturn: PurchaseReturnEntity)

    @Query("DELETE FROM purchase_return_products WHERE purchaseReturnLocalId = :returnId")
    suspend fun deleteItemsForPurchaseReturn(returnId: Long)

    @androidx.room.Transaction
    @Query("SELECT * FROM purchase_returns WHERE NOT isDeletedLocally ORDER BY returnDate DESC")
    fun getAllPurchaseReturnsWithDetailsFlow(): Flow<List<PurchaseReturnWithDetailsEntity>>

    @androidx.room.Transaction
    suspend fun insertPurchaseReturnWithItems(purchaseReturn: PurchaseReturnEntity, items: List<PurchaseReturnProductEntity>): Long {
        val returnId = insertOrUpdatePurchaseReturn(purchaseReturn)
        val itemsWithId = items.map { it.copy(purchaseReturnLocalId = returnId) }
        insertPurchaseReturnItems(itemsWithId)
        return returnId
    }

    @androidx.room.Transaction
    suspend fun updatePurchaseReturnWithItems(purchaseReturn: PurchaseReturnEntity, items: List<PurchaseReturnProductEntity>) {
        updatePurchaseReturn(purchaseReturn)
        deleteItemsForPurchaseReturn(purchaseReturn.localId)
        val itemsWithId = items.map { it.copy(purchaseReturnLocalId = purchaseReturn.localId) }
        insertPurchaseReturnItems(itemsWithId)
    }

    @androidx.room.Transaction
    @Query("SELECT * FROM purchase_returns WHERE localId = :localId")
    suspend fun getPurchaseReturnWithDetails(localId: Long): PurchaseReturnWithDetailsEntity?

    @Query("SELECT * FROM purchase_returns WHERE localId = :localId")
    suspend fun getPurchaseReturnEntityByLocalId(localId: Long): PurchaseReturnEntity?
}
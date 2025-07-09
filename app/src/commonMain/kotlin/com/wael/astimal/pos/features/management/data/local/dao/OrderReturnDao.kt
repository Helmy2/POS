package com.wael.astimal.pos.features.management.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.wael.astimal.pos.features.management.data.local.entity.OrderReturnEntity
import com.wael.astimal.pos.features.management.data.local.entity.OrderReturnProductEntity
import com.wael.astimal.pos.features.management.data.local.entity.OrderReturnWithDetailsEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface OrderReturnDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateReturn(orderReturn: OrderReturnEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturnItems(items: List<OrderReturnProductEntity>)

    @Query("SELECT * FROM order_returns WHERE localId = :localId")
    suspend fun getReturnEntityByLocalId(localId: Long): OrderReturnEntity?

    @Update
    suspend fun updateReturn(orderReturn: OrderReturnEntity)

    @Transaction
    suspend fun updateReturnWithItems(
        orderReturn: OrderReturnEntity,
        items: List<OrderReturnProductEntity>
    ) {
        updateReturn(orderReturn)
        deleteItemsForReturn(orderReturn.localId)
        val itemsWithCorrectId = items.map { it.copy(orderReturnLocalId = orderReturn.localId) }
        if (itemsWithCorrectId.isNotEmpty()) {
            insertReturnItems(itemsWithCorrectId)
        }
    }

    @Query("DELETE FROM order_products WHERE orderLocalId = :orderReturnLocalId")
    suspend fun deleteItemsForReturn(orderReturnLocalId: Long)

    @Transaction
    @Query("SELECT * FROM order_returns WHERE localId = :localId")
    fun getReturnWithDetailsFlow(localId: Long): Flow<OrderReturnWithDetailsEntity?>

    @Transaction
    @Query("SELECT * FROM order_returns WHERE NOT isDeletedLocally")
    fun getAllReturnsWithDetailsFlow(): Flow<List<OrderReturnWithDetailsEntity>>

    @Transaction
    @Query("SELECT * FROM order_returns WHERE businessPartnerLocalId = :clientId AND NOT isDeletedLocally")
    fun getReturnsByClientId(clientId: Long): Flow<List<OrderReturnWithDetailsEntity>>


    @Query("SELECT * FROM order_return_products WHERE orderReturnLocalId = :orderReturnLocalId")
    suspend fun getItemsForReturn(orderReturnLocalId: Long): List<OrderReturnProductEntity>

    @Query("SELECT MAX(invoiceNumber) FROM order_returns WHERE invoiceNumber LIKE :pattern")
    suspend fun getLastInvoiceNumber(pattern: String): String?
}
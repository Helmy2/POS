package com.wael.astimal.pos.features.management.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wael.astimal.pos.features.management.data.entity.OrderReturnEntity
import com.wael.astimal.pos.features.management.data.entity.OrderReturnProductEntity
import com.wael.astimal.pos.features.management.data.entity.OrderReturnWithDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderReturnDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateOrderReturn(orderReturn: OrderReturnEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderReturnItems(items: List<OrderReturnProductEntity>)

    @androidx.room.Transaction
    @Query("SELECT * FROM order_returns WHERE NOT isDeletedLocally ORDER BY returnDate DESC")
    fun getAllOrderReturnsWithDetailsFlow(): Flow<List<OrderReturnWithDetailsEntity>>

    @Query("DELETE FROM order_return_products WHERE orderReturnLocalId = :returnLocalId")
    suspend fun deleteItemsForReturn(returnLocalId: Long)

    @androidx.room.Transaction
    suspend fun updateSalesReturnWithItems(returnEntity: OrderReturnEntity, items: List<OrderReturnProductEntity>) {
        insertOrUpdateOrderReturn(returnEntity)
        deleteItemsForReturn(returnEntity.localId)
        val itemsWithCorrectId = items.map { it.copy(orderReturnLocalId = returnEntity.localId) }
        if (itemsWithCorrectId.isNotEmpty()) {
            insertOrderReturnItems(itemsWithCorrectId)
        }
    }

    @Query("SELECT * FROM order_returns WHERE localId = :localId")
    suspend fun getOrderReturnEntityByLocalId(localId: Long): OrderReturnEntity?
}
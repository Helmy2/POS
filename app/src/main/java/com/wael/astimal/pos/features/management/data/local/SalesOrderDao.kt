package com.wael.astimal.pos.features.management.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.wael.astimal.pos.features.management.data.entity.OrderEntity
import com.wael.astimal.pos.features.management.data.entity.OrderProductEntity
import com.wael.astimal.pos.features.management.data.entity.OrderWithDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesOrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderProductEntity>)

    @Query("SELECT * FROM orders WHERE localId = :localId")
    suspend fun getOrderEntityByLocalId(localId: Long): OrderEntity?

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Transaction
    suspend fun updateOrderWithItems(order: OrderEntity, items: List<OrderProductEntity>) {
        updateOrder(order)
        deleteItemsForOrder(order.localId)
        val itemsWithCorrectId = items.map { it.copy(orderLocalId = order.localId) }
        if (itemsWithCorrectId.isNotEmpty()) {
            insertOrderItems(itemsWithCorrectId)
        }
    }

    @Query("DELETE FROM order_products WHERE orderLocalId = :orderLocalId")
    suspend fun deleteItemsForOrder(orderLocalId: Long)

    @Transaction
    @Query("SELECT * FROM orders WHERE localId = :localId")
    fun getOrderWithDetailsFlow(localId: Long): Flow<OrderWithDetailsEntity?>

    @Transaction
    @Query("SELECT * FROM orders WHERE NOT isDeletedLocally ORDER BY orderDate DESC")
    fun getAllOrdersWithDetailsFlow(): Flow<List<OrderWithDetailsEntity>>
}
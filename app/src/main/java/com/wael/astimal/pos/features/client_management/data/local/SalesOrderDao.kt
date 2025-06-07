package com.wael.astimal.pos.features.client_management.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wael.astimal.pos.features.client_management.data.entity.OrderEntity
import com.wael.astimal.pos.features.client_management.data.entity.OrderProductEntity
import com.wael.astimal.pos.features.client_management.data.entity.OrderWithDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesOrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderProductEntity>)

    @Query("SELECT * FROM orders WHERE localId = :localId")
    suspend fun getOrderEntityByLocalId(localId: Long): OrderEntity?

    @Query("DELETE FROM order_products WHERE orderLocalId = :orderLocalId")
    suspend fun deleteItemsForOrder(orderLocalId: Long)

    @androidx.room.Transaction
    @Query("SELECT * FROM orders WHERE localId = :localId")
    fun getOrderWithDetailsFlow(localId: Long): Flow<OrderWithDetailsEntity?>

    @androidx.room.Transaction
    @Query("SELECT * FROM orders WHERE NOT isDeletedLocally ORDER BY orderDate DESC")
    fun getAllOrdersWithDetailsFlow(): Flow<List<OrderWithDetailsEntity>>
}
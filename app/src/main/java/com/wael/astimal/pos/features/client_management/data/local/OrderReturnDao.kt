package com.wael.astimal.pos.features.client_management.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wael.astimal.pos.features.client_management.data.entity.OrderReturnEntity
import com.wael.astimal.pos.features.client_management.data.entity.OrderReturnProductEntity
import com.wael.astimal.pos.features.client_management.data.entity.OrderReturnWithDetailsEntity
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
}
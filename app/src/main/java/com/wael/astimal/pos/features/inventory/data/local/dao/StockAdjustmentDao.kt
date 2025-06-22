package com.wael.astimal.pos.features.inventory.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.wael.astimal.pos.features.inventory.data.entity.StockAdjustmentEntity

@Dao
interface StockAdjustmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(adjustment: StockAdjustmentEntity): Long

}
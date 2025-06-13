package com.wael.astimal.pos.features.management.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wael.astimal.pos.features.management.data.entity.ReceivePayVoucherEntity
import com.wael.astimal.pos.features.management.data.entity.ReceivePayVoucherWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceivePayVoucherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucher(voucher: ReceivePayVoucherEntity): Long

    @Transaction
    @Query("SELECT * FROM receive_pay_vouchers ORDER BY date DESC")
    fun getAllVouchersWithDetails(): Flow<List<ReceivePayVoucherWithDetails>>
}
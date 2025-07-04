package com.wael.astimal.pos.features.management.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.wael.astimal.pos.features.management.data.entity.ReceivePayVoucherEntity
import com.wael.astimal.pos.features.management.data.entity.ReceivePayVoucherWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceivePayVoucherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucher(voucher: ReceivePayVoucherEntity): Long

    @Transaction
    @Query("SELECT * FROM receive_pay_vouchers WHERE NOT isDeletedLocally")
    fun getAllVouchersWithDetails(): Flow<List<ReceivePayVoucherWithDetails>>

    @Transaction
    @Query("SELECT * FROM receive_pay_vouchers WHERE partnerLocalId = :clientId AND  partyType = \"CLIENT\" AND NOT isDeletedLocally")
    fun getVouchersByClientId(clientId: Long): Flow<List<ReceivePayVoucherWithDetails>>


    @Transaction
    @Query("SELECT * FROM receive_pay_vouchers WHERE partnerLocalId = :supplierId AND  partyType = \"CLIENT\" AND NOT isDeletedLocally")
    fun getVouchersBySupplierId(supplierId: Long): Flow<List<ReceivePayVoucherWithDetails>>

    @Transaction
    @Query("""
        SELECT * FROM receive_pay_vouchers
        WHERE ((partnerLocalId = :clientId AND partyType = "CLIENT")
        OR (partnerLocalId = :supplierId AND partyType = "SUPPLIER"))
        AND NOT isDeletedLocally
    """)
    fun getVouchersByPartnerIds(clientId: Long, supplierId: Long): Flow<List<ReceivePayVoucherWithDetails>>

    @Update
    suspend fun updateVoucher(voucher: ReceivePayVoucherEntity)

    @Query("DELETE FROM receive_pay_vouchers WHERE localId = :voucherId")
    suspend fun deleteVoucher(voucherId: Long)

    @Transaction
    @Query("SELECT * FROM receive_pay_vouchers WHERE localId = :voucherId")
    suspend fun getVoucherWithDetailsById(voucherId: Long): ReceivePayVoucherWithDetails?
}

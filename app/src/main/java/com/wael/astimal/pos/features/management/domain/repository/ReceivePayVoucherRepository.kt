package com.wael.astimal.pos.features.management.domain.repository

import com.wael.astimal.pos.features.management.data.entity.ReceivePayVoucherEntity
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import kotlinx.coroutines.flow.Flow

interface ReceivePayVoucherRepository {
    fun getVouchers(): Flow<List<ReceivePayVoucher>>
    suspend fun addVoucher(voucher: ReceivePayVoucherEntity): Result<Unit>
    suspend fun updateVoucher(voucher: ReceivePayVoucherEntity): Result<Unit>
    suspend fun deleteVoucher(voucherId: Long): Result<Unit>
}

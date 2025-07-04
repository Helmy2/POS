package com.wael.astimal.pos.features.management.domain.repository

import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import kotlinx.coroutines.flow.Flow

interface ReceivePayVoucherRepository {
    fun getVouchers(): Flow<List<ReceivePayVoucher>>
    suspend fun saveVoucher(voucher: ReceivePayVoucher): Result<Unit>
    suspend fun deleteVoucher(voucher: ReceivePayVoucher): Result<Unit>
}

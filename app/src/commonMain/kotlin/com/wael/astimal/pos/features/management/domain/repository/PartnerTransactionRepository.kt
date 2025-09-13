package com.wael.astimal.pos.features.management.domain.repository

import com.wael.astimal.pos.features.management.data.local.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import kotlinx.coroutines.flow.Flow

interface PartnerTransactionRepository {
    fun getVouchers(): Flow<List<ReceivePayVoucher>>

    suspend fun getVoucher(id: String): Result<ReceivePayVoucher>

    suspend fun saveVoucher(voucher: ReceivePayVoucher): Result<Unit>
    suspend fun deleteVoucher(voucher: ReceivePayVoucher): Result<Unit>
    suspend fun syncWithServer(entities: List<PartnerTransactionEntity>): Result<Unit>
    suspend fun getUnsyncedTransactions(): Result<List<ReceivePayVoucher>>
    suspend fun getAllDeletedTransactions(): Result<List<ReceivePayVoucher>>
    suspend fun deleteAll(ids: List<String>): Result<Unit>
}

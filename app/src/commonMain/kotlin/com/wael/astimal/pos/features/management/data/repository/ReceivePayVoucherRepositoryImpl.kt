package com.wael.astimal.pos.features.management.data.repository

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.features.management.data.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.ReceivePayVoucherEntity
import com.wael.astimal.pos.features.management.data.entity.TransactionType
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.PartnerTransactionDao
import com.wael.astimal.pos.features.management.data.local.ReceivePayVoucherDao
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import com.wael.astimal.pos.features.management.domain.entity.VoucherPartyType
import com.wael.astimal.pos.features.management.domain.entity.toEntity
import com.wael.astimal.pos.features.management.domain.repository.ReceivePayVoucherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReceivePayVoucherRepositoryImpl(
    private val voucherDao: ReceivePayVoucherDao,
    private val partnerTransactionDao: PartnerTransactionDao
) : ReceivePayVoucherRepository {

    override fun getVouchers(): Flow<List<ReceivePayVoucher>> {
        return voucherDao.getAllVouchersWithDetails().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveVoucher(voucher: ReceivePayVoucher): Result<Unit> {
        return runCatching {
            if (voucher.id == Id.new) {
                val voucherId = voucherDao.insertVoucher(voucher.toEntity())
                partnerTransactionDao.insertTransaction(
                    voucher.toEntity().toLedgerEntry(voucherId)
                )
            } else {
                voucherDao.updateVoucher(voucher.toEntity())

                // Delete the old ledger entry and insert the updated one
                partnerTransactionDao.deleteTransactionsBySource(
                    voucher.id.local,
                    voucher.getTransactionType()
                )
                partnerTransactionDao.insertTransaction(
                    voucher.toEntity().toLedgerEntry(voucher.id.local)
                )
            }
        }
    }

    override suspend fun deleteVoucher(voucher: ReceivePayVoucher): Result<Unit> {
        return try {
            partnerTransactionDao.deleteTransactionsBySource(
                voucher.id.local,
                voucher.getTransactionType()
            )
            voucherDao.deleteVoucher(voucher.id.local)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun ReceivePayVoucherEntity.toLedgerEntry(voucherId: Long): PartnerTransactionEntity {
    return when (partyType) {
        VoucherPartyType.CLIENT -> PartnerTransactionEntity(
            serverId = null,
            partnerLocalId = partnerLocalId,
            sourceTransactionId = voucherId,
            transactionType = TransactionType.PAYMENT_RECEIVED,
            createdAt = createdAt,
            updatedAt = updatedAt,
            debit = 0.0,
            credit = amount
        )

        VoucherPartyType.SUPPLIER -> PartnerTransactionEntity(
            serverId = null,
            partnerLocalId = partnerLocalId,
            sourceTransactionId = voucherId,
            transactionType = TransactionType.PAYMENT_SENT,
            createdAt = createdAt,
            updatedAt = updatedAt,
            debit = amount,
            credit = 0.0
        )
    }
}

private fun ReceivePayVoucher.getTransactionType(): TransactionType {
    return if (partyType == VoucherPartyType.CLIENT) TransactionType.PAYMENT_RECEIVED else TransactionType.PAYMENT_SENT
}
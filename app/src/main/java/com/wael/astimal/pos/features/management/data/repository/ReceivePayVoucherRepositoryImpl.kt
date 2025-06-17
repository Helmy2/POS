package com.wael.astimal.pos.features.management.data.repository

import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.management.data.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.ReceivePayVoucherEntity
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.PartnerTransactionDao
import com.wael.astimal.pos.features.management.data.local.ReceivePayVoucherDao
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import com.wael.astimal.pos.features.management.domain.entity.VoucherPartyType
import com.wael.astimal.pos.features.management.domain.repository.ReceivePayVoucherRepository
import com.wael.astimal.pos.features.reports.domain.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReceivePayVoucherRepositoryImpl(
    private val database: AppDatabase,
    private val voucherDao: ReceivePayVoucherDao,
    private val partnerTransactionDao: PartnerTransactionDao
) : ReceivePayVoucherRepository {

    override fun getVouchers(): Flow<List<ReceivePayVoucher>> {
        return voucherDao.getAllVouchersWithDetails().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun addVoucher(voucher: ReceivePayVoucherEntity): Result<Unit> {
        return try {
            database.withTransaction {
                val voucherEntity = voucher
                val voucherId = voucherDao.insertVoucher(voucherEntity)
                partnerTransactionDao.insertTransaction(voucher.toLedgerEntry(voucherId))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateVoucher(voucher: ReceivePayVoucherEntity): Result<Unit> {
        return try {
            database.withTransaction {
                val voucherEntity = voucher
                voucherDao.updateVoucher(voucherEntity)

                // Delete the old ledger entry and insert the updated one
                partnerTransactionDao.deleteTransactionsBySource(
                    voucher.localId,
                    voucher.getTransactionType()
                )
                partnerTransactionDao.insertTransaction(voucher.toLedgerEntry(voucher.localId))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteVoucher(voucherId: Long): Result<Unit> {
        return try {
            database.withTransaction {
                // Delete both the voucher and its corresponding ledger entry
                val voucherToDelete =
                    voucherDao.getVoucherWithDetailsById(voucherId) ?: throw NoSuchElementException(
                        "Voucher not found"
                    )
                partnerTransactionDao.deleteTransactionsBySource(
                    voucherId,
                    voucherToDelete.voucher.getTransactionType()
                )
                voucherDao.deleteVoucher(voucherId)
            }
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
            clientId = clientLocalId,
            supplierId = null,
            sourceTransactionId = voucherId,
            transactionType = TransactionType.PAYMENT_RECEIVED,
            createdAt = createdAt,
            updatedAt = updatedAt,
            debit = 0.0,
            credit = amount
        )

        VoucherPartyType.SUPPLIER -> PartnerTransactionEntity(
            serverId = null,
            clientId = null,
            supplierId = supplierLocalId,
            sourceTransactionId = voucherId,
            transactionType = TransactionType.PAYMENT_SENT,
            createdAt = createdAt,
            updatedAt = updatedAt,
            debit = amount,
            credit = 0.0
        )
    }
}

private fun ReceivePayVoucherEntity.getTransactionType(): TransactionType {
    return if (partyType == VoucherPartyType.CLIENT) TransactionType.PAYMENT_RECEIVED else TransactionType.PAYMENT_SENT
}
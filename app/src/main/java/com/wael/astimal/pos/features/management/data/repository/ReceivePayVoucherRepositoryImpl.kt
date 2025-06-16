package com.wael.astimal.pos.features.management.data.repository

import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.management.data.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.ReceivePayVoucherEntity
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.PartnerTransactionDao
import com.wael.astimal.pos.features.management.data.local.ReceivePayVoucherDao
import com.wael.astimal.pos.features.management.domain.entity.Client
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import com.wael.astimal.pos.features.management.domain.entity.Supplier
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

    override suspend fun addVoucher(voucher: ReceivePayVoucher): Result<Unit> {
        return try {
            database.withTransaction {
                val voucherEntity = voucher.toEntity()
                val voucherId = voucherDao.insertVoucher(voucherEntity)
                partnerTransactionDao.insertTransaction(voucher.toLedgerEntry(voucherId))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateVoucher(voucher: ReceivePayVoucher): Result<Unit> {
        return try {
            database.withTransaction {
                val voucherEntity = voucher.toEntity()
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
                    voucherToDelete.toDomain().getTransactionType()
                )
                voucherDao.deleteVoucher(voucherId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Helper mapper functions
private fun ReceivePayVoucher.toEntity(): ReceivePayVoucherEntity {
    return ReceivePayVoucherEntity(
        localId = this.localId,
        serverId = this.serverId,
        amount = this.amount,
        clientLocalId = if (this.party is Client) this.party.id else null,
        supplierLocalId = if (this.party is Supplier) this.party.id else null,
        date = this.date,
        notes = this.notes,
        employeeLocalId = this.createdBy.id,
        isReceipt = this.partyType == VoucherPartyType.CLIENT,
        isSynced = false // Always mark as unsynced on create/update
    )
}

private fun ReceivePayVoucher.toLedgerEntry(voucherId: Long): PartnerTransactionEntity {
    return when (this.partyType) {
        VoucherPartyType.CLIENT -> PartnerTransactionEntity(
            clientId = (this.party as Client).id,
            supplierId = null,
            sourceTransactionId = voucherId,
            transactionType = TransactionType.PAYMENT_RECEIVED,
            date = this.date,
            debit = 0.0,
            credit = this.amount
        )

        VoucherPartyType.SUPPLIER -> PartnerTransactionEntity(
            clientId = null,
            supplierId = (this.party as Supplier).id,
            sourceTransactionId = voucherId,
            transactionType = TransactionType.PAYMENT_SENT,
            date = this.date,
            debit = this.amount,
            credit = 0.0
        )
    }
}

private fun ReceivePayVoucher.getTransactionType(): TransactionType {
    return if (this.partyType == VoucherPartyType.CLIENT) TransactionType.PAYMENT_RECEIVED else TransactionType.PAYMENT_SENT
}


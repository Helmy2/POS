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
                val voucherEntity = ReceivePayVoucherEntity(
                    serverId = null,
                    amount = voucher.amount,
                    clientLocalId = if (voucher.party is Client) voucher.party.id else null,
                    supplierLocalId = if (voucher.party is Supplier) voucher.party.id else null,
                    date = voucher.date,
                    notes = voucher.notes,
                    employeeLocalId = voucher.createdBy.id,
                    isReceipt = voucher.partyType == VoucherPartyType.CLIENT
                )
                val voucherId = voucherDao.insertVoucher(voucherEntity)

                // Create a corresponding entry in the unified transaction ledger
                val transaction = when (voucher.partyType) {
                    VoucherPartyType.CLIENT -> {
                        // A payment received from a client is a CREDIT to their account (reduces what they owe)
                        PartnerTransactionEntity(
                            clientId = (voucher.party as Client).id,
                            supplierId = null,
                            sourceTransactionId = voucherId,
                            transactionType = TransactionType.PAYMENT_RECEIVED,
                            date = voucher.date,
                            debit = 0.0,
                            credit = voucher.amount
                        )
                    }
                    VoucherPartyType.SUPPLIER -> {
                        // A payment sent to a supplier is a DEBIT to their account (reduces what you owe them)
                        PartnerTransactionEntity(
                            clientId = null,
                            supplierId = (voucher.party as Supplier).id,
                            sourceTransactionId = voucherId,
                            transactionType = TransactionType.PAYMENT_SENT,
                            date = voucher.date,
                            debit = voucher.amount,
                            credit = 0.0
                        )
                    }
                }
                partnerTransactionDao.insertTransaction(transaction)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

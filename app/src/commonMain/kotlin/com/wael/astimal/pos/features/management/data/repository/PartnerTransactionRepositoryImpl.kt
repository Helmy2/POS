package com.wael.astimal.pos.features.management.data.repository

import com.wael.astimal.pos.features.management.data.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.PartnerTransactionDao
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import com.wael.astimal.pos.features.management.domain.repository.PartnerTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PartnerTransactionRepositoryImpl(
    private val transactionDao: PartnerTransactionDao,
) : PartnerTransactionRepository {

    override fun getVouchers(): Flow<List<ReceivePayVoucher>> {
        return transactionDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveVoucher(voucher: ReceivePayVoucher): Result<Unit> {
        return runCatching {
//            if (voucher.id == Id.new) {
//                val voucherId = voucherDao.insertVoucher(voucher.toEntity())
//                partnerTransactionDao.insertTransaction(
//                    voucher.toEntity().toLedgerEntry(voucherId)
//                )
//            } else {
//                voucherDao.updateVoucher(voucher.toEntity())
//
//                // Delete the old ledger entry and insert the updated one
//                partnerTransactionDao.deleteTransactionsBySource(
//                    voucher.id.local,
//                    voucher.getTransactionType()
//                )
//                partnerTransactionDao.insertTransaction(
//                    voucher.toEntity().toLedgerEntry(voucher.id.local)
//                )
//            }
        }
    }

    override suspend fun deleteVoucher(voucher: ReceivePayVoucher): Result<Unit> {
        return try {
//            partnerTransactionDao.deleteTransactionsBySource(
//                voucher.id.local,
//                voucher.getTransactionType()
//            )
//            voucherDao.deleteVoucher(voucher.id.local)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncWithServer(entities: List<PartnerTransactionEntity>): Result<Unit> {
        return runCatching {
            entities.map { serverEntity ->
                val existingLocal = transactionDao.getTransactionBySeverId(
                    serverEntity.serverId ?: throw Exception("serverId is null")
                )

                serverEntity.copy(
                    localId = existingLocal?.localId ?: 0L,
                )
            }.forEach {
                transactionDao.insertOrUpdate(it)
            }
        }
    }
}
//
//private fun ReceivePayVoucherEntity.toLedgerEntry(voucherId: Long): PartnerTransactionEntity {
//    return when (partyType) {
//        VoucherPartyType.CLIENT -> PartnerTransactionEntity(
//            serverId = null,
//            partnerLocalId = partnerLocalId,
//            sourceTransactionId = voucherId,
//            transactionType = TransactionType.PAYMENT_RECEIVED,
//            createdAt = createdAt,
//            updatedAt = updatedAt,
//            debit = 0.0,
//            credit = amount
//        )
//
//        VoucherPartyType.SUPPLIER -> PartnerTransactionEntity(
//            serverId = null,
//            partnerLocalId = partnerLocalId,
//            sourceTransactionId = voucherId,
//            transactionType = TransactionType.PAYMENT_SENT,
//            createdAt = createdAt,
//            updatedAt = updatedAt,
//            debit = amount,
//            credit = 0.0
//        )
//    }
//}
//
//private fun ReceivePayVoucher.getTransactionType(): TransactionType {
//    return if (partyType == VoucherPartyType.CLIENT) TransactionType.PAYMENT_RECEIVED else TransactionType.PAYMENT_SENT
//}
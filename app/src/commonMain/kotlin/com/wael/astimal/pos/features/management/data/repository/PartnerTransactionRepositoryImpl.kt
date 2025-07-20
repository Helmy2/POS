package com.wael.astimal.pos.features.management.data.repository

import com.wael.astimal.pos.features.management.data.local.dao.PartnerTransactionDao
import com.wael.astimal.pos.features.management.data.local.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.data.local.entity.toDomain
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import com.wael.astimal.pos.features.management.domain.entity.toEntity
import com.wael.astimal.pos.features.management.domain.repository.PartnerTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PartnerTransactionRepositoryImpl(
    private val transactionDao: PartnerTransactionDao,
) : PartnerTransactionRepository {

    override fun getVouchers(): Flow<List<ReceivePayVoucher>> {
        return transactionDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun saveVoucher(voucher: ReceivePayVoucher): Result<Unit> {
        return runCatching {
            val toInsert = if (voucher.id == "") {
                voucher.toEntity().copy(localId = Uuid.random().toString())
            } else {
                voucher.toEntity()
            }
            transactionDao.insertOrUpdate(toInsert)
        }
    }

    override suspend fun deleteVoucher(voucher: ReceivePayVoucher): Result<Unit> {
        return try {
            transactionDao.softDeleteTransactionById(voucher.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncWithServer(entities: List<PartnerTransactionEntity>): Result<Unit> {
        return runCatching {
            transactionDao.deleteAll()
            entities.forEach {
                transactionDao.insertOrUpdate(it)
            }
        }
    }

    override suspend fun getUnsyncedTransactions(): Result<List<ReceivePayVoucher>> {
        return runCatching {
            transactionDao.getUnsyncedTransactions().map { it.toDomain() }
        }
    }

    override suspend fun getAllDeletedTransactions(): Result<List<ReceivePayVoucher>> {
        return runCatching {
            transactionDao.getAllDeletedTransactions().map { it.toDomain() }
        }
    }

    override suspend fun hardDeleteByServerId(serverId: String): Result<Unit> {
        return runCatching {
            transactionDao.hardDeleteTransactionById(serverId)
        }
    }
}
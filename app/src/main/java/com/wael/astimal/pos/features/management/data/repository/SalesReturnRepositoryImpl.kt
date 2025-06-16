package com.wael.astimal.pos.features.management.data.repository

import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.management.data.entity.OrderProductEntity
import com.wael.astimal.pos.features.management.data.entity.OrderReturnEntity
import com.wael.astimal.pos.features.management.data.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.OrderReturnDao
import com.wael.astimal.pos.features.management.data.local.PartnerTransactionDao
import com.wael.astimal.pos.features.management.data.logic.ReturnAmountLogic
import com.wael.astimal.pos.features.management.domain.entity.SalesReturn
import com.wael.astimal.pos.features.management.domain.repository.SalesReturnRepository
import com.wael.astimal.pos.features.reports.domain.entity.TransactionType
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SalesReturnRepositoryImpl(
    private val database: AppDatabase,
    private val orderReturnDao: OrderReturnDao,
    private val returnAmountLogic: ReturnAmountLogic,
    private val partnerTransactionDao: PartnerTransactionDao,
    private val sessionManager: SessionManager
) : SalesReturnRepository {

    override fun getReturns(query: String): Flow<List<SalesReturn>> {
        return orderReturnDao.getAllReturnsWithDetailsFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getReturnDetailsFlow(returnLocalId: Long): Flow<SalesReturn?> {
        return orderReturnDao.getReturnWithDetailsFlow(returnLocalId).map { entityWithDetails ->
            entityWithDetails?.takeUnless { it.orderReturn.isDeletedLocally }?.toDomain()
        }
    }

    override suspend fun addReturn(
        returnEntity: OrderReturnEntity,
        items: List<OrderProductEntity>
    ): Result<SalesReturn> {
        return try {
            var insertedReturnLocalId: Long = -1
            database.withTransaction {
                insertedReturnLocalId = orderReturnDao.insertOrUpdateReturn(returnEntity)
                val itemsWithCorrectId = items.map { it.copy(orderLocalId = insertedReturnLocalId) }
                orderReturnDao.insertReturnItems(itemsWithCorrectId)

                returnAmountLogic.processNewReturn(returnEntity, items, insertedReturnLocalId)
                addReturnLedgerEntries(returnEntity, insertedReturnLocalId)
            }
            val createdReturn = getReturnDetailsFlow(insertedReturnLocalId).first()
                ?: return Result.failure(IllegalStateException("Failed to retrieve return after insert."))
            Result.success(createdReturn)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateReturn(
        returnEntity: OrderReturnEntity,
        items: List<OrderProductEntity>
    ): Result<SalesReturn> {
        return try {
            val returnId = returnEntity.localId
            database.withTransaction {
                val currentUserId = sessionManager.getCurrentUser().first()?.id
                    ?: throw Exception("User not authenticated")

                val oldReturnEntity = orderReturnDao.getReturnEntityByLocalId(returnId)
                    ?: throw NoSuchElementException("Original return not found")
                val oldItems = orderReturnDao.getItemsForReturn(returnId)

                returnAmountLogic.revertReturn(oldReturnEntity, oldItems, currentUserId)
                partnerTransactionDao.deleteTransactionsBySource(
                    returnId,
                    TransactionType.SALE_RETURN,
                    TransactionType.PAYMENT_SENT
                )

                val entityToUpdate = returnEntity.copy(isSynced = false, lastModified = System.currentTimeMillis())
                orderReturnDao.updateReturnWithItems(entityToUpdate, items)

                returnAmountLogic.processNewReturn(entityToUpdate, items, returnId)
                addReturnLedgerEntries(entityToUpdate, returnId)
            }
            val updatedReturn = getReturnDetailsFlow(returnId).first()
                ?: return Result.failure(IllegalStateException("Failed to retrieve return after update."))
            Result.success(updatedReturn)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteReturn(returnLocalId: Long): Result<Unit> {
        return try {
            database.withTransaction {
                val currentUserId = sessionManager.getCurrentUser().first()?.id
                    ?: throw Exception("User not authenticated")

                val returnEntity = orderReturnDao.getReturnEntityByLocalId(returnLocalId)
                    ?: throw NoSuchElementException("Return not found")

                if (!returnEntity.isDeletedLocally) {
                    val items = orderReturnDao.getItemsForReturn(returnLocalId)
                    returnAmountLogic.revertReturn(returnEntity, items, currentUserId)
                    partnerTransactionDao.deleteTransactionsBySource(
                        returnLocalId,
                        TransactionType.SALE_RETURN,
                        TransactionType.PAYMENT_SENT
                    )

                    val returnToMarkAsDeleted = returnEntity.copy(
                        isDeletedLocally = true,
                        isSynced = false,
                        lastModified = System.currentTimeMillis()
                    )
                    orderReturnDao.updateReturn(returnToMarkAsDeleted)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun addReturnLedgerEntries(returnEntity: OrderReturnEntity, returnId: Long) {
        // Create ledger entry for the sales return
        partnerTransactionDao.insertTransaction(
            PartnerTransactionEntity(
                clientId = returnEntity.clientLocalId,
                supplierId = null,
                sourceTransactionId = returnId,
                transactionType = TransactionType.SALE_RETURN,
                date = returnEntity.returnDate,
                debit = 0.0,
                credit = returnEntity.totalAmount // A sales return is a credit to the client
            )
        )
        // If you refunded money, it's a debit to the client's account
        if (returnEntity.amountPaid > 0) {
            partnerTransactionDao.insertTransaction(
                PartnerTransactionEntity(
                    clientId = returnEntity.clientLocalId,
                    supplierId = null,
                    sourceTransactionId = returnId,
                    transactionType = TransactionType.PAYMENT_SENT,
                    date = returnEntity.returnDate,
                    debit = returnEntity.amountPaid,
                    credit = 0.0
                )
            )
        }
    }
}
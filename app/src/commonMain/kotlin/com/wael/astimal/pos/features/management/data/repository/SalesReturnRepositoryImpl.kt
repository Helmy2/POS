package com.wael.astimal.pos.features.management.data.repository

import com.wael.astimal.pos.core.util.formatSequence
import com.wael.astimal.pos.features.management.data.entity.OrderReturnEntity
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.OrderReturnDao
import com.wael.astimal.pos.features.management.data.local.PartnerTransactionDao
import com.wael.astimal.pos.features.management.data.logic.ReturnAmountLogic
import com.wael.astimal.pos.features.management.domain.entity.SalesReturn
import com.wael.astimal.pos.features.management.domain.entity.toEntity
import com.wael.astimal.pos.features.management.domain.repository.SalesReturnRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class SalesReturnRepositoryImpl(
    private val orderReturnDao: OrderReturnDao,
    private val returnAmountLogic: ReturnAmountLogic,
    private val partnerTransactionDao: PartnerTransactionDao,
    private val userRepository: UserRepository
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

    private suspend fun generateNextInvoiceNumber(): String {
        val random = Random.nextInt(1, 999999)
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val prefix = "INV-RET-$today-$random-"
        val lastInvoice = orderReturnDao.getLastInvoiceNumber("$prefix%")
        val nextSeq = if (lastInvoice == null) {
            1
        } else {
            lastInvoice.substringAfter(prefix).toIntOrNull()?.plus(1) ?: 1
        }
        return "$prefix${nextSeq.formatSequence()}"
    }

    override suspend fun addReturn(
        salesReturn: SalesReturn
    ): Result<SalesReturn> {
        return try {
            val (returnEntity, items) = salesReturn.toEntity()
            val newInvoiceNumber = generateNextInvoiceNumber()
            val returnWithInvoice = returnEntity.copy(invoiceNumber = newInvoiceNumber)

            val insertedReturnLocalId: Long = orderReturnDao.insertOrUpdateReturn(returnWithInvoice)
            val itemsWithCorrectId =
                items.map { it.copy(orderReturnLocalId = insertedReturnLocalId) }
            orderReturnDao.insertReturnItems(itemsWithCorrectId)

            returnAmountLogic.processNewReturn(returnWithInvoice, items, insertedReturnLocalId)
            addReturnLedgerEntries(returnWithInvoice, insertedReturnLocalId)
            val createdReturn =
                getReturnDetailsFlow(insertedReturnLocalId).first() ?: return Result.failure(
                    IllegalStateException("Failed to retrieve return after insert.")
                )
            Result.success(createdReturn)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateReturn(
        salesReturn: SalesReturn
    ): Result<SalesReturn> {
        return try {
//            val (returnEntity, items) = salesReturn.toEntity()
//
//            val returnId = returnEntity.localId
//            val currentUserId =
//                userRepository.getCurrentUser()?.id ?: throw Exception("User not authenticated")
//
//            val oldReturnEntity = orderReturnDao.getReturnEntityByLocalId(returnId)
//                ?: throw NoSuchElementException("Original return not found")
//            val oldItems = orderReturnDao.getItemsForReturn(returnId)
//
//            returnAmountLogic.revertReturn(oldReturnEntity, oldItems, currentUserId.local)
//            partnerTransactionDao.deleteTransactionsBySource(
//                returnId, TransactionType.SALE_RETURN, TransactionType.PAYMENT_SENT
//            )
//
//            val entityToUpdate = returnEntity.copy(isSynced = false, updatedAt = Clock.now())
//            orderReturnDao.updateReturnWithItems(entityToUpdate, items)
//
//            returnAmountLogic.processNewReturn(entityToUpdate, items, returnId)
//            addReturnLedgerEntries(entityToUpdate, returnId)
//            val updatedReturn = getReturnDetailsFlow(returnId).first() ?: return Result.failure(
//                IllegalStateException("Failed to retrieve return after update.")
//            )
//            Result.success(updatedReturn)
            TODO()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteReturn(returnLocalId: Long): Result<Unit> {
        return try {
//            val currentUserId =
//                userRepository.getCurrentUser()?.id ?: throw Exception("User not authenticated")
//
//            val returnEntity = orderReturnDao.getReturnEntityByLocalId(returnLocalId)
//                ?: throw NoSuchElementException("Return not found")
//
//            if (!returnEntity.isDeletedLocally) {
//                val items = orderReturnDao.getItemsForReturn(returnLocalId)
//                returnAmountLogic.revertReturn(returnEntity, items, currentUserId.local)
//                partnerTransactionDao.deleteTransactionsBySource(
//                    returnLocalId, TransactionType.SALE_RETURN, TransactionType.PAYMENT_SENT
//                )
//
//                val returnToMarkAsDeleted = returnEntity.copy(
//                    isDeletedLocally = true, isSynced = false, updatedAt = Clock.now()
//                )
//                orderReturnDao.updateReturn(returnToMarkAsDeleted)
//            }
//            Result.success(Unit)
            TODO()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun addReturnLedgerEntries(returnEntity: OrderReturnEntity, returnId: Long) {
        // Create ledger entry for the sales return
//        partnerTransactionDao.insertTransaction(
//            PartnerTransactionEntity(
//                serverId = null,
//                partnerLocalId = returnEntity.businessPartnerLocalId,
//                sourceTransactionId = returnId,
//                transactionType = TransactionType.SALE_RETURN,
//                createdAt = returnEntity.createdAt,
//                updatedAt = returnEntity.updatedAt,
//                debit = 0.0,
//                credit = returnEntity.totalAmount // A sales return is a credit to the client
//            )
//        )
//        // If you refunded money, it's a debit to the client's account
//        if (returnEntity.amountPaid > 0) {
//            partnerTransactionDao.insertTransaction(
//                PartnerTransactionEntity(
//                    serverId = null,
//                    partnerLocalId = returnEntity.businessPartnerLocalId,
//                    sourceTransactionId = returnId,
//                    transactionType = TransactionType.PAYMENT_SENT,
//                    createdAt = returnEntity.createdAt,
//                    updatedAt = returnEntity.updatedAt,
//                    debit = returnEntity.amountPaid,
//                    credit = 0.0
//                )
//            )
//        }
    }
}
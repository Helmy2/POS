package com.wael.astimal.pos.features.client_management.data.repository

import com.wael.astimal.pos.features.client_management.data.entity.OrderReturnEntity
import com.wael.astimal.pos.features.client_management.data.entity.OrderReturnProductEntity
import com.wael.astimal.pos.features.client_management.data.entity.toDomain
import com.wael.astimal.pos.features.client_management.data.local.OrderReturnDao
import com.wael.astimal.pos.features.client_management.domain.entity.SalesReturn
import com.wael.astimal.pos.features.client_management.domain.repository.SalesReturnRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class SalesReturnRepositoryImpl(
    private val orderReturnDao: OrderReturnDao,
) : SalesReturnRepository {

    override fun getSalesReturns(): Flow<List<SalesReturn>> {
        return orderReturnDao.getAllOrderReturnsWithDetailsFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getSalesReturnDetailsFlow(returnLocalId: Long): Flow<SalesReturn?> {
        // Assuming a similar DAO method exists for single item flow
        // For now, we can filter the main list, but a direct DAO call is better.
        return getSalesReturns().map { returns ->
            returns.find { it.localId == returnLocalId }
        }
    }

    override suspend fun addSalesReturn(
        returnEntity: OrderReturnEntity,
        items: List<OrderReturnProductEntity>
    ): Result<SalesReturn> {
        return try {
            // This should ideally happen in a single database transaction.
            // You can add @Transaction to the DAO method that calls these.
            val insertedReturnLocalId = orderReturnDao.insertOrUpdateOrderReturn(returnEntity)

            val itemsWithCorrectId = items.map { it.copy(orderReturnLocalId = insertedReturnLocalId) }
            orderReturnDao.insertOrderReturnItems(itemsWithCorrectId)

            // Fetch the newly created return with all its details to return the domain object
            // Assuming a method `getOrderReturnWithDetails(localId)` exists in the DAO
            // For now, we'll return a manually constructed object as a placeholder.
            // val createdReturnWithDetails = orderReturnDao.getOrderReturnWithDetails(insertedReturnLocalId)
            //     ?: return Result.failure(IllegalStateException("Failed to retrieve sales return after insert."))
            // Result.success(createdReturnWithDetails.toDomain(getCurrentLanguage()))

            // Placeholder return until DAO has the single-item getter
            Result.success(
                SalesReturn(
                    localId = insertedReturnLocalId,
                    serverId = returnEntity.serverId,
                    invoiceNumber = returnEntity.invoiceNumber,
                    clientLocalId = returnEntity.clientLocalId,
                    clientName = null, // Needs to be fetched
                    supplierLocalId = returnEntity.supplierLocalId,
                    supplierName = null, // Needs to be fetched
                    employeeLocalId = returnEntity.employeeLocalId,
                    employeeName = null, // Needs to be fetched
                    previousDebt = returnEntity.previousDebt,
                    amountPaid = returnEntity.amountPaid,
                    amountRemaining = returnEntity.amountRemaining,
                    totalReturnedValue = returnEntity.totalReturnedValue,
                    totalGainLoss = returnEntity.totalGainLoss,
                    paymentType = returnEntity.paymentType,
                    returnDate = returnEntity.returnDate,
                    items = emptyList(), // Items are saved, but not re-fetched in this simplified version
                    isSynced = returnEntity.isSynced,
                    lastModified = returnEntity.lastModified,
                    isDeletedLocally = returnEntity.isDeletedLocally
                )
            )

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSalesReturn(returnLocalId: Long): Result<Unit> {
        // todo
        // Implement soft delete logic similar to OrderRepository
        // 1. Fetch the OrderReturnEntity by localId
        // 2. Copy it with isDeletedLocally = true and isSynced = false
        // 3. Call insertOrUpdateOrderReturn() with the updated entity
        println("SalesReturnRepositoryImpl: deleteSalesReturn() called. Logic to be implemented.")
        return Result.success(Unit)
    }

}
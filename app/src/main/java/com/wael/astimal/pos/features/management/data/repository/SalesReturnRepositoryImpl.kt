package com.wael.astimal.pos.features.management.data.repository

import androidx.room.withTransaction
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.management.data.entity.OrderReturnEntity
import com.wael.astimal.pos.features.management.data.entity.OrderReturnProductEntity
import com.wael.astimal.pos.features.management.data.entity.toDomain
import com.wael.astimal.pos.features.management.data.local.OrderReturnDao
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.SalesReturn
import com.wael.astimal.pos.features.management.domain.repository.ClientRepository
import com.wael.astimal.pos.features.management.domain.repository.SalesReturnRepository
import com.wael.astimal.pos.features.user.data.local.EmployeeDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SalesReturnRepositoryImpl(
    private val database: AppDatabase, // For transactions
    private val salesReturnDao: OrderReturnDao,
    private val employeeDao: EmployeeDao,
    private val clientRepository: ClientRepository,
    private val stockRepository: StockRepository,
) : SalesReturnRepository {
    

    override fun getSalesReturns(): Flow<List<SalesReturn>> {
        return salesReturnDao.getAllOrderReturnsWithDetailsFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getSalesReturnDetailsFlow(returnLocalId: Long): Flow<SalesReturn?> {
        return salesReturnDao.getOrderReturnWithDetailsFlow(returnLocalId).map { entity ->
            entity?.takeUnless { it.orderReturn.isDeletedLocally }?.toDomain()
        }
    }

    override suspend fun addSalesReturn(
        returnEntity: OrderReturnEntity,
        items: List<OrderReturnProductEntity>
    ): Result<SalesReturn> {
        return try {
            var insertedId: Long = -1
            database.withTransaction {
                val employeeId = returnEntity.employeeLocalId
                    ?: throw Exception("Employee ID is missing on the return record.")
                val employeeStoreId = employeeDao.getStoreIdForEmployee(employeeId)
                    ?: throw Exception("Could not find an assigned store for the employee.")

                insertedId = salesReturnDao.insertSalesReturnWithItems(returnEntity, items)

                items.forEach { item ->
                    stockRepository.adjustStock(
                        storeId = employeeStoreId,
                        productId = item.productLocalId,
                        transactionUnitId = item.unitLocalId,
                        transactionQuantity = item.quantity // INCREASE stock for a sales return
                    )
                }

                if (returnEntity.clientLocalId != null && returnEntity.paymentType == PaymentType.DEFERRED) {
                    clientRepository.adjustClientDebt(
                        clientLocalId = returnEntity.clientLocalId,
                        changeInDebt = -returnEntity.totalReturnedValue // DECREASE client debt
                    )
                }
            }
            val createdReturn = salesReturnDao.getOrderReturnWithDetails(insertedId)
                ?: return Result.failure(IllegalStateException("Failed to retrieve sales return after insert."))
            Result.success(createdReturn.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSalesReturn(
        returnEntity: OrderReturnEntity,
        items: List<OrderReturnProductEntity>
    ): Result<SalesReturn> {
        return try {
            if (returnEntity.localId == 0L) {
                return Result.failure(IllegalArgumentException("Sales Return localId is missing for update."))
            }

            database.withTransaction {
                val employeeId = returnEntity.employeeLocalId ?: throw Exception("Employee ID is missing.")
                val employeeStoreId = employeeDao.getStoreIdForEmployee(employeeId) ?: throw Exception("Employee's store not found.")

                val oldReturnWithDetails = salesReturnDao.getOrderReturnWithDetails(returnEntity.localId)
                if (oldReturnWithDetails != null) {
                    // Revert old adjustments
                    oldReturnWithDetails.itemsWithProductDetails.forEach { oldItem ->
                        stockRepository.adjustStock(
                            storeId = employeeStoreId,
                            productId = oldItem.returnItem.productLocalId,
                            transactionUnitId = oldItem.returnItem.unitLocalId,
                            transactionQuantity = -oldItem.returnItem.quantity // Decrease stock to revert
                        )
                    }
                    if(oldReturnWithDetails.orderReturn.clientLocalId != null && oldReturnWithDetails.orderReturn.paymentType == PaymentType.DEFERRED) {
                        clientRepository.adjustClientDebt(oldReturnWithDetails.orderReturn.clientLocalId, oldReturnWithDetails.orderReturn.totalReturnedValue) // Increase debt to revert
                    }
                }

                val entityToUpdate = returnEntity.copy(isSynced = false, lastModified = System.currentTimeMillis())
                salesReturnDao.updateSalesReturnWithItems(entityToUpdate, items)

                // Apply new adjustments
                items.forEach { newItem ->
                    stockRepository.adjustStock(
                        storeId = employeeStoreId,
                        productId = newItem.productLocalId,
                        transactionUnitId = newItem.unitLocalId,
                        transactionQuantity = newItem.quantity // Increase stock
                    )
                }
                if (entityToUpdate.clientLocalId != null && entityToUpdate.paymentType == PaymentType.DEFERRED) {
                    clientRepository.adjustClientDebt(entityToUpdate.clientLocalId, -entityToUpdate.totalReturnedValue) // Decrease debt
                }
            }
            val updatedReturn = salesReturnDao.getOrderReturnWithDetails(returnEntity.localId)
                ?: return Result.failure(IllegalStateException("Failed to retrieve sales return after update."))
            Result.success(updatedReturn.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSalesReturn(returnLocalId: Long): Result<Unit> {
        return try {
            database.withTransaction {
                val returnToDelete = salesReturnDao.getOrderReturnWithDetails(returnLocalId)
                    ?: throw NoSuchElementException("Sales Return not found with localId: $returnLocalId")

                if (!returnToDelete.orderReturn.isDeletedLocally) {
                    val employeeId = returnToDelete.orderReturn.employeeLocalId ?: throw Exception("Employee ID missing.")
                    val employeeStoreId = employeeDao.getStoreIdForEmployee(employeeId) ?: throw Exception("Employee's store not found.")

                    // Revert stock and debt adjustments
                    returnToDelete.itemsWithProductDetails.forEach { item ->
                        stockRepository.adjustStock(
                            storeId = employeeStoreId,
                            productId = item.returnItem.productLocalId,
                            transactionUnitId = item.returnItem.unitLocalId,
                            transactionQuantity = -item.returnItem.quantity // DECREASE stock to revert the return
                        )
                    }
                    if (returnToDelete.orderReturn.clientLocalId != null && returnToDelete.orderReturn.paymentType == PaymentType.DEFERRED) {
                        clientRepository.adjustClientDebt(
                            clientLocalId = returnToDelete.orderReturn.clientLocalId,
                            changeInDebt = returnToDelete.orderReturn.totalReturnedValue // INCREASE client debt to revert
                        )
                    }

                    val entityToMarkAsDeleted = returnToDelete.orderReturn.copy(
                        isDeletedLocally = true,
                        isSynced = false,
                        lastModified = System.currentTimeMillis()
                    )
                    salesReturnDao.insertOrUpdateOrderReturn(entityToMarkAsDeleted)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
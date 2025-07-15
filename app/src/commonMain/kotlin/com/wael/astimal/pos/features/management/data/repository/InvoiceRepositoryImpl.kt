package com.wael.astimal.pos.features.management.data.repository


import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.data.local.dao.StockAdjustmentDao
import com.wael.astimal.pos.features.inventory.data.local.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import com.wael.astimal.pos.features.management.data.local.dao.EmployeeFinancesDao
import com.wael.astimal.pos.features.management.data.local.dao.InvoiceDao
import com.wael.astimal.pos.features.management.data.local.dao.PartnerTransactionDao
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceEntity
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceItemEntity
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceType
import com.wael.astimal.pos.features.management.data.local.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.data.local.entity.TransactionType
import com.wael.astimal.pos.features.management.data.local.entity.toDomain
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransaction
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransactionType
import com.wael.astimal.pos.features.management.domain.entity.Invoice
import com.wael.astimal.pos.features.management.domain.entity.toEntity
import com.wael.astimal.pos.features.management.domain.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class InvoiceRepositoryImpl(
    private val invoiceDao: InvoiceDao,
    private val partnerTransactionDao: PartnerTransactionDao,
    private val stockAdjustmentDao: StockAdjustmentDao,
    private val employeeFinancesDao: EmployeeFinancesDao
) : InvoiceRepository {

    override fun getInvoices(): Flow<List<Invoice>> {
        return invoiceDao.getAllInvoicesWithItems().map { items -> items.map { it.toDomain() } }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun addSalesOrder(invoice: Invoice): Result<Unit> {
        return try {
            val newInvoice = invoice.copy(id = Uuid.random().toString())
            val invoiceWithItems = newInvoice.toEntity()

            invoiceDao.insertInvoiceWithItems(
                invoiceWithItems.first,
                invoiceWithItems.second.map { it },
            )

            createAdjustStock(newInvoice)
            adjustPartnerTransactions(newInvoice)
            makeCommission(newInvoice)

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private suspend fun makeCommission(invoice: Invoice) {
        val invoiceCommission = when (invoice.invoiceType) {
            InvoiceType.SALES -> invoice.totalAmount * 25 / 100
            InvoiceType.PURCHASE_RETURN -> invoice.totalAmount * -25 / 100
            InvoiceType.PURCHASE -> return
            InvoiceType.SALES_RETURN -> return
        }

        val transaction1 = EmployeeTransaction(
            id = Id.new,
            employee = invoice.employee,
            amount = invoiceCommission,
            createdAt = Clock.now(),
            updatedAt = Clock.now(),
            type = EmployeeTransactionType.COMMISSION,
            invoiceId = invoice.id,
            notes = null,
            createdByEmployee = invoice.employee
        )
        val transaction2 = EmployeeTransaction(
            id = Id.new,
            employee = invoice.partner.responsibleEmployee,
            amount = invoiceCommission,
            createdAt = Clock.now(),
            updatedAt = Clock.now(),
            type = EmployeeTransactionType.COMMISSION,
            invoiceId = invoice.id,
            notes = null,
            createdByEmployee = invoice.employee
        )

        employeeFinancesDao.insertOrUpdate(transaction1.toEntity())
        employeeFinancesDao.insertOrUpdate(transaction2.toEntity())
    }

    override suspend fun updateOrder(invoice: Invoice): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteSalesOrder(orderId: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun adjustPartnerTransactions(invoice: Invoice) {
        val price =
            if (invoice.invoiceType == InvoiceType.SALES || invoice.invoiceType == InvoiceType.PURCHASE_RETURN) (invoice.totalAmount - invoice.paidAmount)
            else -(invoice.totalAmount - invoice.paidAmount)

        val transaction = PartnerTransactionEntity(
            serverId = Uuid.random().toString(),
            partnerLocalId = invoice.partner.id.local,
            employeeLocalId = invoice.employee.id.local,
            invoiceId = invoice.id,
            transactionType = TransactionType.INVOICE,
            createdAt = Clock.now(),
            updatedAt = Clock.now(),
            balance = price,
            notes = null
        )

        partnerTransactionDao.insertOrUpdate(transaction)
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun createAdjustStock(
        invoice: Invoice
    ) {
        for (item in invoice.items) {
            val quantity =
                if (invoice.invoiceType == InvoiceType.SALES || invoice.invoiceType == InvoiceType.PURCHASE_RETURN) -item.quantity
                else item.quantity

            stockAdjustmentDao.insert(
                StockAdjustmentEntity(
                    serverId = Uuid.random().toString(),
                    productId = item.product.id.local,
                    quantityChange = quantity,
                    createdAt = Clock.now(),
                    updatedAt = Clock.now(),
                    userId = invoice.employee.id.local,
                    reason = StockAdjustmentReason.INVOICE,
                    storeId = invoice.store.id.local,
                    invoiceId = invoice.id,
                    isSynced = false,
                    notes = null,
                )
            )
        }
    }

    override suspend fun getUnsyncedInvoices(): Result<List<Invoice>> {
        return runCatching {
            invoiceDao.getUnsyncedInvoices().map { it.toDomain() }
        }
    }

    override suspend fun getAllDeletedInvoice(): Result<List<Invoice>> {
        return runCatching {
            invoiceDao.getDeletedInvoice().map { it.toDomain() }
        }
    }

    override suspend fun hardDeleteInvoice(id: String): Result<Unit> {
        return runCatching {
            invoiceDao.hardDeleteInvoiceById(id)
        }
    }

    override suspend fun syncInvoices(entities: List<InvoiceEntity>): Result<Unit> {
        return runCatching {
            entities.forEach {
                invoiceDao.insertInvoice(it)
            }
        }
    }

    override suspend fun getUnsyncedInvoicesItems(): Result<List<InvoiceItemEntity>> {
        return runCatching {
            invoiceDao.getUnsyncedInvoicesItems()
        }
    }

    override suspend fun getAllDeletedInvoiceItems(): Result<List<InvoiceItemEntity>> {
        return runCatching {
            invoiceDao.getDeletedInvoiceItems()
        }
    }

    override suspend fun hardDeleteInvoiceItems(id: String): Result<Unit> {
        return runCatching {
            invoiceDao.hardDeleteInvoiceItemsById(id)
        }
    }

    override suspend fun syncInvoicesItems(entities: List<InvoiceItemEntity>): Result<Unit> {
        return runCatching {
            entities.forEach {
                invoiceDao.insertInvoiceInvoice(it)
            }
        }
    }
}

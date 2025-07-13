package com.wael.astimal.pos.features.management.data.repository


import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.data.local.dao.StockAdjustmentDao
import com.wael.astimal.pos.features.inventory.data.local.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import com.wael.astimal.pos.features.management.data.local.dao.InvoiceDao
import com.wael.astimal.pos.features.management.data.local.dao.PartnerTransactionDao
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceType
import com.wael.astimal.pos.features.management.data.local.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.data.local.entity.TransactionType
import com.wael.astimal.pos.features.management.data.local.entity.toDomain
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
            // Todo employee Commission

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
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
            if (invoice.invoiceType == InvoiceType.SALES || invoice.invoiceType == InvoiceType.PURCHASE_RETURN)
                (invoice.totalAmount - invoice.paidAmount)
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
}

package com.wael.astimal.pos.features.management.data.repository


import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.data.local.dao.StockAdjustmentDao
import com.wael.astimal.pos.features.inventory.data.local.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.management.data.local.dao.InvoiceDao
import com.wael.astimal.pos.features.management.data.local.dao.PartnerTransactionDao
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceType
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceWithItems
import com.wael.astimal.pos.features.management.data.local.entity.toDomain
import com.wael.astimal.pos.features.management.domain.entity.Invoice
import com.wael.astimal.pos.features.management.domain.entity.toEntity
import com.wael.astimal.pos.features.management.domain.repository.InvoiceRepository
import com.wael.astimal.pos.features.user.domain.entity.User
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class InvoiceRepositoryImpl(
    private val invoiceDao: InvoiceDao,
    private val partnerTransactionDao: PartnerTransactionDao,
    private val stockRepository: StockRepository,
    private val userRepository: UserRepository,
    private val stockAdjustmentDao: StockAdjustmentDao,

    ) : InvoiceRepository {

    override fun getInvoices(): Flow<List<Invoice>> {
        return invoiceDao.getAllInvoicesWithItems().map { items -> items.map { it.toDomain() } }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun addSalesOrder(invoice: Invoice): Result<Unit> {
        return try {
            val invoiceWithItems = invoice.toEntity()

            val supabaseId = Uuid.random().toString()
            val invoiceLocalId = invoiceDao.insertInvoiceWithItems(
                invoiceWithItems.first.copy(
                    supabaseId = supabaseId,
                ),
                invoiceWithItems.second.map { it },
            )

            val currentUser = userRepository.getCurrentUser() ?: throw Exception("User not found")

            createAdjustStock(
                currentUser, invoice.copy(
                    id = invoice.id.copy(
                        local = invoiceLocalId, serverStringId = supabaseId
                    )
                )
            )
//            adjustPartnerTransactions(invoiceLocalId, invoiceWithItems)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun adjustPartnerTransactions(
        invoiceLocalId: Long, invoiceWithItems: InvoiceWithItems
    ) {
//        // 3. Create financial ledger entries
//        // Debit the client's account for the total amount of the sale
//        val debitTransaction = PartnerTransactionEntity(
//            businessPartnerId = invoice.client.id.local,
//            invoiceId = newInvoiceId.toString(), // Link to the new invoice
//            transactionType = PartnerTransactionType.INVOICE,
//            debit = invoice.totalAmount,
//            credit = 0.0,
//            notes = "Sale Invoice #${invoice.invoiceNumber}",
//            createdAt = clock.now(),
//            updatedAt = clock.now(),
//            serverId = null
//        )
//        partnerTransactionDao.insert(debitTransaction)
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun createAdjustStock(
        currentUser: User, invoice: Invoice
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
                    userId = currentUser.id.local,
                    reason = StockAdjustmentReason.INVOICE,
                    storeId = invoice.store.id.local,
                    invoiceId = invoice.id.local,
                    isSynced = false,
                    notes = null,
                )
            )
        }
    }
}

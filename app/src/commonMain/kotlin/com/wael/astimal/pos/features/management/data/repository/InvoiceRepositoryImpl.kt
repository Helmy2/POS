package com.wael.astimal.pos.features.management.data.repository


import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.dashboard.domain.entity.DailySale
import com.wael.astimal.pos.features.inventory.data.local.dao.ProductDao
import com.wael.astimal.pos.features.inventory.data.local.dao.StockAdjustmentDao
import com.wael.astimal.pos.features.inventory.data.local.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
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
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class InvoiceRepositoryImpl(
    private val invoiceDao: InvoiceDao,
    private val partnerTransactionDao: PartnerTransactionDao,
    private val stockAdjustmentDao: StockAdjustmentDao,
    private val productDao: ProductDao,
    private val stockRepository: StockRepository,
    private val employeeFinancesDao: EmployeeFinancesDao,
    private val userRepository: UserRepository,
    private val supabaseClient: SupabaseClient
) : InvoiceRepository {

    override fun getInvoices(): Flow<List<Invoice>> {
        return invoiceDao.getAllInvoicesWithItems().map { items -> items.map { it.toDomain() } }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun addSalesOrder(invoice: Invoice): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val newInvoice = invoice.copy(id = Uuid.random().toString())
                val invoiceWithItems = newInvoice.toEntity()

                invoiceDao.insertInvoiceWithItems(
                    invoiceWithItems.first,
                    invoiceWithItems.second.map { it },
                )

                updateProductsAveragePrice(invoice)

                createAdjustStock(newInvoice)
                adjustPartnerTransactions(newInvoice)
                makeCommission(newInvoice)

                Result.success(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    private suspend fun updateProductsAveragePrice(invoice: Invoice) {
        if (invoice.invoiceType == InvoiceType.SALES || invoice.invoiceType == InvoiceType.SALES_RETURN) {
            return
        }

        invoice.items.forEach { item ->
            val currentCost = productDao.getAverageCost(item.product.id)
            val currentQuantity = stockRepository.getStockQuantity(productId = item.product.id)

            if (item.quantity <= 0) return@forEach

            val newCost = if (invoice.invoiceType == InvoiceType.PURCHASE) {
                val newTotalQuantity = currentQuantity + item.quantity
                if (newTotalQuantity > 0) {
                    ((currentCost * currentQuantity) + (item.unitPrice * item.quantity)) / newTotalQuantity
                } else {
                    currentCost
                }
            } else { // PURCHASE_RETURN
                val newTotalQuantity = currentQuantity - item.quantity
                if (newTotalQuantity <= 0) {
                    currentCost
                } else {
                    ((currentCost * currentQuantity) - (item.unitPrice * item.quantity)) / newTotalQuantity
                }
            }
            productDao.updateAverageCost(item.product.id, newCost)
        }
    }

    private suspend fun revertProductsAveragePrice(invoice: Invoice) {
        if (invoice.invoiceType == InvoiceType.SALES || invoice.invoiceType == InvoiceType.SALES_RETURN) {
            return
        }

        invoice.items.forEach { item ->
            val currentCost = productDao.getAverageCost(item.product.id)
            val currentQuantity = stockRepository.getStockQuantity(productId = item.product.id)

            if (item.quantity <= 0) return@forEach

            val revertedCost = if (invoice.invoiceType == InvoiceType.PURCHASE) {
                val originalQuantity = currentQuantity - item.quantity
                if (originalQuantity <= 0) {
                    0.0
                } else {
                    ((currentCost * currentQuantity) - (item.unitPrice * item.quantity)) / originalQuantity
                }
            } else { // PURCHASE_RETURN
                val originalQuantity = currentQuantity + item.quantity
                if (originalQuantity > 0) {
                    ((currentCost * currentQuantity) + (item.unitPrice * item.quantity)) / originalQuantity
                } else {
                    currentCost
                }
            }
            productDao.updateAverageCost(item.product.id, revertedCost)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun makeCommission(invoice: Invoice) {
        if (invoice.invoiceType == InvoiceType.PURCHASE) return
        if (invoice.invoiceType == InvoiceType.PURCHASE_RETURN) return

        val isOrder = invoice.invoiceType == InvoiceType.SALES

        val invoicePurchaseTotalPrice =
            invoice.items.sumOf {
                productDao.getAverageCost(it.product.id) * it.quantity
            }

        val profit = if (isOrder)
            invoice.totalAmount - invoicePurchaseTotalPrice
        else invoicePurchaseTotalPrice - invoice.totalAmount


        val admin = userRepository.getAdmin() ?: throw Exception("Admin not found")

        val transaction1 = EmployeeTransaction(
            id = Uuid.random().toString(),
            employee = invoice.employee,
            amount = profit * 0.25,
            createdAt = Clock.now(),
            updatedAt = Clock.now(),
            type = if (isOrder) EmployeeTransactionType.COMMISSION_FOR_ORDER else EmployeeTransactionType.COMMISSION_FOR_RETURN_ORDER,
            invoiceId = invoice.id,
            notes = null,
            createdByEmployee = invoice.employee
        )
        val transaction2 = EmployeeTransaction(
            id = Uuid.random().toString(),
            employee = invoice.partner.responsibleEmployee,
            amount = profit * 0.25,
            createdAt = Clock.now(),
            updatedAt = Clock.now(),
            type = if (isOrder) EmployeeTransactionType.COMMISSION_FOR_RESPONSIBILITY_FOR_ORDER else EmployeeTransactionType.COMMISSION_FOR_RESPONSIBILITY_FOR_RETURN_ORDER,
            invoiceId = invoice.id,
            notes = null,
            createdByEmployee = invoice.employee
        )
        val transaction3 = EmployeeTransaction(
            id = Uuid.random().toString(),
            employee = admin,
            amount = profit * 0.5,
            createdAt = Clock.now(),
            updatedAt = Clock.now(),
            type = if (isOrder) EmployeeTransactionType.COMMISSION_TO_ADMIN_FOR_ORDER else EmployeeTransactionType.COMMISSION_TO_ADMIN_FOR_RETURN_ORDER,
            invoiceId = invoice.id,
            notes = null,
            createdByEmployee = invoice.employee
        )

        employeeFinancesDao.insertOrUpdate(transaction1.toEntity())
        employeeFinancesDao.insertOrUpdate(transaction2.toEntity())
        employeeFinancesDao.insertOrUpdate(transaction3.toEntity())
    }

    override suspend fun updateOrder(invoice: Invoice): Result<Unit> {
        return runCatching {
            deleteSalesOrder(invoice.id)
            addSalesOrder(invoice)
        }
    }

    override suspend fun deleteSalesOrder(orderId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val invoiceToDelete = invoiceDao.getInvoiceById(orderId).toDomain()
                revertProductsAveragePrice(invoiceToDelete)

                supabaseClient.from("invoice_items").delete { filter { eq("invoice_id", orderId) } }
                supabaseClient.from("stock_adjustments")
                    .delete { filter { eq("invoice_id", orderId) } }
                supabaseClient.from("partner_transactions")
                    .delete { filter { eq("invoice_id", orderId) } }
                supabaseClient.from("employee_transactions")
                    .delete { filter { eq("invoice_id", orderId) } }
                supabaseClient.from("invoices").delete { filter { eq("id", orderId) } }

                stockAdjustmentDao.deleteAdjustmentsByInvoiceId(orderId)
                partnerTransactionDao.deleteTransactionsByInvoiceId(orderId)
                employeeFinancesDao.deleteTransactionsByInvoiceId(orderId)
                invoiceDao.deleteInvoiceWithItemsById(orderId)
                Result.success(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun adjustPartnerTransactions(invoice: Invoice) {
        val price =
            when (invoice.invoiceType) {
                InvoiceType.SALES, InvoiceType.PURCHASE_RETURN -> (invoice.totalAmount - invoice.paidAmount)
                else -> -(invoice.totalAmount - invoice.paidAmount)
            }

        val transaction = PartnerTransactionEntity(
            localId = Uuid.random().toString(),
            partnerLocalId = invoice.partner.id,
            employeeLocalId = invoice.employee.id,
            invoiceId = invoice.id,
            transactionType = when (invoice.invoiceType) {
                InvoiceType.SALES -> TransactionType.SALE_INVOICE
                InvoiceType.PURCHASE -> TransactionType.PURCHASE_INVOICE
                InvoiceType.SALES_RETURN -> TransactionType.SALE_RETURN_INVOICE
                InvoiceType.PURCHASE_RETURN -> TransactionType.PURCHASE_RETURN_INVOICE
            },
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
                    localId = Uuid.random().toString(),
                    productId = item.product.id,
                    quantityChange = quantity,
                    createdAt = Clock.now(),
                    updatedAt = Clock.now(),
                    userId = invoice.employee.id,
                    reason = StockAdjustmentReason.INVOICE,
                    storeId = invoice.store.id,
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
            invoiceDao.deleteAllInvoices()
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


    override suspend fun hardDeleteInvoiceItems(id: String): Result<Unit> {
        return runCatching {
            invoiceDao.hardDeleteInvoiceItemsById(id)
        }
    }

    override suspend fun syncInvoicesItems(entities: List<InvoiceItemEntity>): Result<Unit> {
        return runCatching {
            invoiceDao.deleteAllInvoiceItems()
            entities.forEach {
                invoiceDao.insertInvoiceInvoice(it)
            }
        }.onFailure {
            it.printStackTrace()
        }
    }

    override fun getDailySales(startMillis: Long, endMillis: Long): Flow<List<DailySale>> {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        return invoiceDao.getDailySales(startMillis, endMillis).map { dailyDataList ->
            dailyDataList.map { dailyData ->
                DailySale(
                    date = LocalDate.parse(dailyData.saleDate, formatter),
                    totalRevenue = dailyData.totalRevenue,
                    numberOfSales = dailyData.numberOfSales
                )
            }
        }
    }

    override suspend fun getInvoiceById(id: String): Result<Invoice> {
        return runCatching {
            invoiceDao.getInvoiceById(id).toDomain()
        }
    }
}

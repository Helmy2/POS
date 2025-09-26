package com.wael.astimal.pos.features.reports.data.repository

import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.core.util.toEpochMillis
import com.wael.astimal.pos.features.inventory.data.local.entity.toDomain
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import com.wael.astimal.pos.features.management.data.local.entity.toDomain
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransactionType
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.management.domain.repository.InvoiceRepository
import com.wael.astimal.pos.features.reports.domain.model.ClientDebitInfo
import com.wael.astimal.pos.features.reports.domain.model.CurrentStockInfo
import com.wael.astimal.pos.features.reports.domain.model.DetailedTransaction
import com.wael.astimal.pos.features.reports.domain.model.EmployeeActivity
import com.wael.astimal.pos.features.reports.domain.model.EmployeeLedgerEntry
import com.wael.astimal.pos.features.reports.domain.model.ProductMovementEntry
import com.wael.astimal.pos.features.reports.domain.model.ProductMovementGroup
import com.wael.astimal.pos.features.reports.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class ReportRepositoryImpl(
    val businessPartnerRepository: BusinessPartnerRepository,
    val db: AppDatabase
) : ReportRepository {
    override suspend fun getClientsWithDebit(
        responsibleEmployeeId: String?
    ): Flow<List<ClientDebitInfo>> {
        return businessPartnerRepository.getBusinessPartners("").map { allPartners ->
            allPartners.filter { responsibleEmployeeId == null || it.responsibleEmployee.id == responsibleEmployeeId }
                .map {
                    val balance = db.partnerTransactionDao().getPartnerBalance(it.id) ?: 0.0
                    ClientDebitInfo(
                        client = it, debitAmount = balance
                    )
                }.filter { it.debitAmount > 0.0 }
        }
    }

    override fun getCurrentStock(
        productId: String?, storeId: String?
    ): Flow<List<CurrentStockInfo>> {
        // This Flow will re-calculate whenever stock adjustments change.
        return db.stockAdjustmentDao().getAll().map { list -> list.map { it.toDomain() } }
            .map { allAdjustments ->
                // Apply the user's filters first
                val filteredAdjustments =
                    allAdjustments.filter { storeId == null || it.store.id == storeId }
                        .filter { productId == null || it.product.id == productId }

                // Group the adjustments by both product and store to get unique stock entries
                val groupedByProductAndStore = filteredAdjustments.groupBy {
                    Pair(it.product.id, it.store.id)
                }

                // Map each group to a CurrentStockInfo object with the summed quantity
                groupedByProductAndStore.map { (_, adjustments) ->
                    val first = adjustments.first()
                    val totalQuantity = adjustments.sumOf { it.quantityChange }

                    CurrentStockInfo(
                        product = first.product, store = first.store, quantity = totalQuantity
                    )
                }.filter { it.quantity != 0.0 }
            }
    }

    @OptIn(ExperimentalTime::class)
    override fun getTransactionsForPartner(
        partnerId: String, startDate: LocalDateTime, endDate: LocalDateTime
    ): Flow<List<DetailedTransaction>> {
        val startEpochMilli = startDate.toEpochMillis()
        val endEpochMilli = endDate.toEpochMillis()

        return db.partnerTransactionDao()
            .getTransactionsForPartnerInRange(partnerId, startEpochMilli, endEpochMilli)
            .map { list ->
                list.map { entity -> entity.toDomain() }.map { entity ->
                    DetailedTransaction(
                        id = entity.id,
                        date = Instant.fromEpochMilliseconds(
                            entity.createdAt
                        ).toLocalDateTime(TimeZone.currentSystemDefault()),
                        transactionType = entity.transactionType,
                        invoiceId = entity.invoiceId.toString(),
                        totalAmount = entity.amount,
                        partnerName = entity.partner.name
                    )
                }.sortedByDescending { it.date }
            }
    }

    override fun getEmployeeLedger(
        employeeId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
    ): Flow<List<EmployeeLedgerEntry>> {
        val startEpochMilli = startDate.toEpochMillis()
        val endEpochMilli = endDate.toEpochMillis()

        return db.employeeFinancesDao()
            .getTransactionsForEmployeeInRange(employeeId, startEpochMilli, endEpochMilli)
            .map { it.map { it.toDomain() } }.map { allTransactions ->

                val transactionsInRange = allTransactions.sortedBy { it.createdAt }

                val ledgerEntries = mutableListOf<EmployeeLedgerEntry>()
                var currentBalance = 0.0

                transactionsInRange.forEach { trx ->
                    currentBalance += trx.amount
                    ledgerEntries.add(
                        EmployeeLedgerEntry(
                            invoiceId = trx.invoiceId,
                            date = Instant.fromEpochMilliseconds(trx.createdAt)
                                .toLocalDateTime(TimeZone.currentSystemDefault()),
                            transactionType = trx.type,
                            notes = trx.notes ?: "",
                            debit = if (trx.amount < 0) abs(trx.amount) else 0.0,
                            credit = if (trx.amount > 0) trx.amount else 0.0,
                            balance = currentBalance
                        )
                    )
                }
                ledgerEntries
            }
    }

    override fun getEmployeeActivityForDateRange(
        employeeId: String, startDate: LocalDateTime, endDate: LocalDateTime
    ): Flow<List<EmployeeActivity>> {
        val startEpochMilli = startDate.toEpochMillis()
        val endEpochMilli = endDate.toEpochMillis()

        // Flow 1: Get all invoices created by the employee
        val invoicesFlow = db.invoiceDao()
            .getInvoicesCreatedByEmployeeInRange(employeeId, startEpochMilli, endEpochMilli)

        // Flow 2: Get only payment/receipt vouchers created by the employee
        val vouchersFlow = db.employeeFinancesDao()
            .getTransactionsForEmployeeInRange(employeeId, startEpochMilli, endEpochMilli)
            .map { entities ->
                entities.filter {
                    it.transactionEntity.type == EmployeeTransactionType.SALARY || it.transactionEntity.type == EmployeeTransactionType.DEDUCTION || it.transactionEntity.type == EmployeeTransactionType.ADVANCE || it.transactionEntity.type == EmployeeTransactionType.BONUS
                }.map { it.toDomain() }
            }

        val partnerTransactionsFlow = db.partnerTransactionDao()
            .getTransactionsCreatedByEmployeeInRange(employeeId, startEpochMilli, endEpochMilli)

        // Combine both flows, transform the data, and return a single sorted list
        return combine(
            invoicesFlow, vouchersFlow, partnerTransactionsFlow
        ) { invoices, vouchers, partnerTransactions ->
            val invoiceActivities = invoices.map { EmployeeActivity.InvoiceActivity(it.toDomain()) }
            val financialActivities = vouchers.map { EmployeeActivity.FinancialActivity(it) }
            val partnerPaymentActivities =
                partnerTransactions.map { EmployeeActivity.PartnerPaymentActivity(it.toDomain()) }

            (invoiceActivities + financialActivities + partnerPaymentActivities).sortedByDescending { it.timestamp }
        }
    }

    override fun getProductMovement(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        productId: String?,
        storeId: String?,
    ): Flow<List<ProductMovementGroup>> {
        val startEpochMilli = startDate.toEpochMillis()
        val endEpochMilli = endDate.toEpochMillis()

        return db.stockAdjustmentDao().getAll().map { list -> list.map { it.toDomain() } }
            .map { allAdjustments ->
                // 1. Apply primary filters for store and product if they exist
                val filteredAdjustments =
                    allAdjustments.filter { storeId == null || it.store.id == storeId }
                        .filter { productId == null || it.product.id == productId }

                // 2. Group all filtered adjustments by the product ID
                val groupedByProduct = filteredAdjustments.groupBy { it.product.id }

                // 3. Process each product group individually
                groupedByProduct.mapNotNull { (_, adjustmentsForProduct) ->
                    val product = adjustmentsForProduct.first().product

                    // Filter transactions within the date range for this product
                    val adjustmentsInRange =
                        adjustmentsForProduct.filter { it.createdAt in startEpochMilli..endEpochMilli }
                            .sortedBy { it.createdAt }

                    val movementEntries = mutableListOf<ProductMovementEntry>()
                    var currentBalance = 0.0


                    // Create ledger entries with a running balance for this product
                    adjustmentsInRange.forEach { adjustment ->
                        currentBalance += adjustment.quantityChange
                        movementEntries.add(
                            ProductMovementEntry(
                                date = Instant.fromEpochMilliseconds(adjustment.createdAt)
                                    .toLocalDateTime(TimeZone.currentSystemDefault()).date,
                                productName = adjustment.product.name,
                                storeName = adjustment.store.name,
                                reason = adjustment.reason,
                                quantityIn = if (adjustment.quantityChange > 0) adjustment.quantityChange else 0.0,
                                quantityOut = if (adjustment.quantityChange < 0) -adjustment.quantityChange else 0.0,
                                balance = currentBalance
                            )
                        )
                    }
                    if (movementEntries.isNotEmpty()) ProductMovementGroup(
                        productName = product.name,
                        entries = movementEntries,
                        totalIn = movementEntries.sumOf { it.quantityIn },
                        totalOut = movementEntries.sumOf { it.quantityOut },
                        closingBalance = currentBalance
                    ) else null
                }
            }
    }

    override fun getStockTransfers(
        startDate: LocalDateTime, endDate: LocalDateTime, fromStoreId: String?, toStoreId: String?
    ): Flow<List<StockTransfer>> {
        val startEpochMilli = startDate.toEpochMillis()
        val endEpochMilli = endDate.toEpochMillis()

        return db.stockTransferDao().getAllStockTransfersWithDetailsFlow().map { allTransfers ->
            allTransfers.filter { it.transfer.createdAt in startEpochMilli..endEpochMilli }
                .filter { fromStoreId == null || it.transfer.fromStoreId == fromStoreId }
                .filter { toStoreId == null || it.transfer.toStoreId == toStoreId }
                .map { it.toDomain() }.sortedByDescending { it.createdAt }
        }
    }
}
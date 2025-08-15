package com.wael.astimal.pos.features.reports.data.repository

import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.management.data.local.entity.toDomain
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransactionType
import com.wael.astimal.pos.features.reports.domain.model.EmployeeActivity
import com.wael.astimal.pos.features.reports.domain.repository.EmployeeReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.time.ExperimentalTime
import kotlin.time.toJavaInstant

@OptIn(ExperimentalTime::class)
class EmployeeReportRepositoryImpl(
    private val db: AppDatabase
) : EmployeeReportRepository {

    override fun getEmployeeActivityForDateRange(
        employeeId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<EmployeeActivity>> {
        val startEpochMilli = startDate.atStartOfDayIn(TimeZone.UTC).toJavaInstant().toEpochMilli()
        val endEpochMilli =
            endDate.atTime(23, 59, 59).toInstant(TimeZone.UTC).toJavaInstant().toEpochMilli()

        // Flow 1: Get all invoices created by the employee
        val invoicesFlow = db.invoiceDao()
            .getInvoicesCreatedByEmployeeInRange(employeeId, startEpochMilli, endEpochMilli)

        // Flow 2: Get only payment/receipt vouchers created by the employee
        val vouchersFlow = db.employeeFinancesDao()
            .getTransactionsForEmployeeInRange(employeeId, startEpochMilli, endEpochMilli)
            .map { entities ->
                entities.filter {
                    it.transactionEntity.type == EmployeeTransactionType.SALARY ||
                            it.transactionEntity.type == EmployeeTransactionType.DEDUCTION ||
                            it.transactionEntity.type == EmployeeTransactionType.ADVANCE ||
                            it.transactionEntity.type == EmployeeTransactionType.BONUS
                }.map { it.toDomain() }
            }

        val partnerTransactionsFlow = db.partnerTransactionDao()
            .getTransactionsCreatedByEmployeeInRange(employeeId, startEpochMilli, endEpochMilli)

        // Combine both flows, transform the data, and return a single sorted list
        return combine(
            invoicesFlow,
            vouchersFlow,
            partnerTransactionsFlow
        ) { invoices, vouchers, partnerTransactions ->
            val invoiceActivities = invoices.map { EmployeeActivity.InvoiceActivity(it.toDomain()) }
            val financialActivities = vouchers.map { EmployeeActivity.FinancialActivity(it) }
            val partnerPaymentActivities =
                partnerTransactions.map { EmployeeActivity.PartnerPaymentActivity(it.toDomain()) }

            (invoiceActivities + financialActivities + partnerPaymentActivities).sortedByDescending { it.timestamp }
        }
    }
}
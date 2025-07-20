package com.wael.astimal.pos.features.reports.data.repository

import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.management.data.local.entity.toDomain
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransactionType
import com.wael.astimal.pos.features.reports.domain.model.EmployeeProfitSummary
import com.wael.astimal.pos.features.reports.domain.repository.ProfitReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime

class ProfitReportRepositoryImpl(
    private val db: AppDatabase
) : ProfitReportRepository {

    override fun getEmployeeProfitSummary(
        employeeId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<EmployeeProfitSummary>> {
        val startEpochMilli = startDate.atStartOfDayIn(TimeZone.UTC).toJavaInstant().toEpochMilli()
        val endEpochMilli =
            endDate.atTime(23, 59, 59).toInstant(TimeZone.UTC).toJavaInstant().toEpochMilli()

        // 1. Fetch all transactions for the employee in the date range.
        return db.employeeFinancesDao().getTransactionsForEmployeeInRange(
            employeeId = employeeId,
            start = startEpochMilli,
            end = endEpochMilli
        ).map { it.map { it.toDomain() } }
            .map { allTransactions ->
                // 2. Filter for commission transactions only.
                val commissionTransactions = allTransactions.filter {
                    it.type == EmployeeTransactionType.COMMISSION_FOR_ORDER ||
                            it.type == EmployeeTransactionType.COMMISSION_FOR_RESPONSIBILITY
                }

                // 3. Group transactions by date.
                val groupedByDate = commissionTransactions.groupBy {
                    Instant.fromEpochMilliseconds(it.createdAt).toLocalDateTime(TimeZone.UTC).date
                }

                // 4. Process each day's transactions to calculate the summary.
                groupedByDate.map { (date, dailyTransactions) ->
                    val directCommission = dailyTransactions
                        .filter { it.type == EmployeeTransactionType.COMMISSION_FOR_ORDER }
                        .sumOf { it.amount }

                    val responsibilityCommission = dailyTransactions
                        .filter { it.type == EmployeeTransactionType.COMMISSION_FOR_RESPONSIBILITY }
                        .sumOf { it.amount }

                    EmployeeProfitSummary(
                        date = date,
                        directCommission = directCommission,
                        responsibilityCommission = responsibilityCommission,
                        totalCommission = directCommission + responsibilityCommission
                    )
                }.sortedByDescending { it.date }
            }
    }
}
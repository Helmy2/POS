package com.wael.astimal.pos.features.reports.domain.repository

import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface EmployeeReportRepository {
    fun getEmployeeTransactionsForDate(
        employeeId: String, startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<EmployeeTransaction>>
}

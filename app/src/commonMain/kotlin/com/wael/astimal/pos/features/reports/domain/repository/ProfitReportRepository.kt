package com.wael.astimal.pos.features.reports.domain.repository

import com.wael.astimal.pos.features.reports.domain.model.EmployeeProfitSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ProfitReportRepository {
    fun getEmployeeProfitSummary(
        employeeId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<EmployeeProfitSummary>>
}
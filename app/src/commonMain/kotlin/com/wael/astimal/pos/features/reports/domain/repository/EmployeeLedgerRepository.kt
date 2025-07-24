package com.wael.astimal.pos.features.reports.domain.repository

import com.wael.astimal.pos.features.reports.domain.model.EmployeeLedgerEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface EmployeeLedgerRepository {
    fun getEmployeeLedger(
        employeeId: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<List<EmployeeLedgerEntry>>
}
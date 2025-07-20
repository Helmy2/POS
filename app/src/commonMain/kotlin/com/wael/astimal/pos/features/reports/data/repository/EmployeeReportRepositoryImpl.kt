package com.wael.astimal.pos.features.reports.data.repository

import com.wael.astimal.pos.features.management.data.local.dao.EmployeeFinancesDao
import com.wael.astimal.pos.features.management.data.local.entity.toDomain
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransaction
import com.wael.astimal.pos.features.reports.domain.repository.EmployeeReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant

class EmployeeReportRepositoryImpl(
    private val employeeFinancesDao: EmployeeFinancesDao
) : EmployeeReportRepository {

    override fun getEmployeeTransactionsForDate(
        employeeId: String, startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<EmployeeTransaction>> {
        val startOfDay = startDate.atStartOfDayIn(TimeZone.UTC).toJavaInstant().toEpochMilli()
        val endOfDay =
            endDate.atTime(23, 59, 59).toInstant(TimeZone.UTC).toJavaInstant().toEpochMilli()

        return employeeFinancesDao.getTransactionsForEmployeeInRange(
            employeeId, startOfDay, endOfDay
        ).map {
            it.map { it.toDomain() }
        }
    }
}
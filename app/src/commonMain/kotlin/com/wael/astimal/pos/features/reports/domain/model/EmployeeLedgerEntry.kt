package com.wael.astimal.pos.features.reports.domain.model

import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransactionType
import kotlinx.datetime.LocalDate

data class EmployeeLedgerEntry(
    val invoiceId: String?,
    val date: LocalDate,
    val transactionType: EmployeeTransactionType,
    val notes: String,
    val debit: Double,
    val credit: Double,
    val balance: Double,
)
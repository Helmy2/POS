package com.wael.astimal.pos.features.reports.domain.model

import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransactionType
import kotlinx.datetime.LocalDateTime

data class EmployeeLedgerEntry(
    val invoiceId: String?,
    val date: LocalDateTime,
    val transactionType: EmployeeTransactionType,
    val notes: String,
    val debit: Double,
    val credit: Double,
    val balance: Double,
)
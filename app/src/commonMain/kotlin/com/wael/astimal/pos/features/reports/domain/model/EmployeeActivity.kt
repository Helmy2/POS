package com.wael.astimal.pos.features.reports.domain.model

import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransaction
import com.wael.astimal.pos.features.management.domain.entity.Invoice
import kotlinx.datetime.Instant


sealed class EmployeeActivity {
    abstract val id: String
    abstract val timestamp: Instant
    abstract val details: String
    abstract val amount: Double
    abstract val type: String

    data class InvoiceActivity(val invoice: Invoice) : EmployeeActivity() {
        override val id: String get() = invoice.id
        override val timestamp: Instant get() = Instant.fromEpochMilliseconds(invoice.createdAt)
        override val details: String get() = "Invoice #${invoice.id} to ${invoice.partner.name}"
        override val amount: Double get() = invoice.totalAmount
        override val type: String get() = invoice.invoiceType.name
    }

    data class FinancialActivity(val transaction: EmployeeTransaction) : EmployeeActivity() {
        override val id: String get() = transaction.id
        override val timestamp: Instant get() = Instant.fromEpochMilliseconds(transaction.createdAt)
        override val details: String
            get() = transaction.notes ?: transaction.type.name.replace(
                '_',
                ' '
            )
        override val amount: Double get() = transaction.amount
        override val type: String get() = transaction.type.name
    }
}
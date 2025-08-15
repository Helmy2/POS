package com.wael.astimal.pos.features.reports.domain.model

import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransaction
import com.wael.astimal.pos.features.management.domain.entity.Invoice
import com.wael.astimal.pos.features.management.domain.entity.ReceivePayVoucher
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@OptIn(ExperimentalTime::class)
sealed class EmployeeActivity {
    abstract val id: String
    abstract val timestamp: Instant

    data class InvoiceActivity(val invoice: Invoice) : EmployeeActivity() {
        override val id: String get() = invoice.id

        override val timestamp: Instant get() = Instant.fromEpochMilliseconds(invoice.createdAt)
    }

    data class FinancialActivity(val transaction: EmployeeTransaction) : EmployeeActivity() {
        override val id: String get() = transaction.id
        override val timestamp: Instant get() = Instant.fromEpochMilliseconds(transaction.createdAt)
    }

    data class PartnerPaymentActivity(val transaction: ReceivePayVoucher) : EmployeeActivity() {
        override val id: String get() = transaction.id
        override val timestamp: Instant get() = Instant.fromEpochMilliseconds(transaction.createdAt)
    }
}
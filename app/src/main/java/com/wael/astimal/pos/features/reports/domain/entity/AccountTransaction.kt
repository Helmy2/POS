package com.wael.astimal.pos.features.reports.domain.entity

import java.time.LocalDateTime


data class AccountTransaction(
    val date: LocalDateTime,
    val transactionId: String,
    val transactionType: TransactionType,
    val invoiceNumber: String,
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val balance: Double = 0.0
)

enum class TransactionType {
    OPENING_BALANCE,
    SALE,
    PURCHASE,
    SALE_RETURN,
    PURCHASE_RETURN,
    PAYMENT_RECEIVED,
    PAYMENT_SENT
}
